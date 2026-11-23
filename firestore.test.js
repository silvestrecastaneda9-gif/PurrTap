const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_silvestre";
const ADMIN_EMAIL = "silvestrecastaneda9@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

function validProfile(uid, overrides = {}) {
  return {
    userId: uid,
    displayName: "WhiskerPro",
    purrs: 100,
    totalPurrsEarned: 100,
    totalPets: 25,
    selectedCatName: "Mochi",
    selectedPatternId: "tabby",
    selectedAccessoryId: "none",
    selectedBackgroundId: "cozy_parlor",
    unlockedPatterns: ["tabby"],
    unlockedAccessories: ["none"],
    unlockedBackgrounds: ["cozy_parlor"],
    upgradeLevels: { feather_wand: 1 },
    dailyStreak: 1,
    lastDailyClaimEpochDay: 20000,
    claimedQuestIds: [],
    unlockedBadgeIds: ["first_purr"],
    equippedBadgeId: "first_purr",
    eventPurrsEarned: 0,
    preferredCity: "San Francisco",
    weatherCheckedCount: 1,
    statsPublic: true,
    bio: "Purr caretaker",
    avatarBase64: "",
    bannerBase64: "",
    createdAt: pastTimestamp(),
    updatedAt: pastTimestamp(),
    ...overrides,
  };
}

function validLeaderboard(uid, overrides = {}) {
  return {
    userId: uid,
    displayName: "WhiskerPro",
    catName: "Mochi",
    catPatternId: "tabby",
    catAccessoryId: "none",
    equippedBadgeId: "first_purr",
    totalPurrsEarned: 100,
    totalPets: 25,
    badgesCount: 1,
    statsPublic: true,
    bio: "Purr caretaker",
    avatarBase64: "",
    bannerBase64: "",
    publicRebirths: 1,
    publicAscensions: 0,
    publicDailyStreak: 4,
    publicHighestComboX100: 215,
    updatedAt: pastTimestamp(),
    ...overrides,
  };
}

function validModeration(targetUid, overrides = {}) {
  return {
    userId: targetUid,
    status: "WARNED",
    reason: "Please be kind in caretaker names.",
    timeoutUntilMillis: 0,
    warningCount: 1,
    updatedByEmail: ADMIN_EMAIL,
    updatedAt: pastTimestamp(),
    ...overrides,
  };
}

function validAnnouncement(messageId, overrides = {}) {
  return {
    messageId,
    title: "Festival Boost",
    body: "Golden meteor shower event starts tonight!",
    severity: "EVENT",
    senderEmail: ADMIN_EMAIL,
    createdAt: pastTimestamp(),
    ...overrides,
  };
}

function validCounter(itemId, overrides = {}) {
  return {
    itemId,
    itemType: "PATTERN",
    existCount: 1,
    updatedAt: pastTimestamp(),
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write users, leaderboard, moderation, announcements, or counters", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("leaderboard").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(unauthDb.collection("moderation").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("global_messages").get());
  await assertFails(unauthDb.collection("limited_item_stats").get());
});

test("Authenticated user can create, read, update, and delete own profile for Danger Zone reset", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(docRef.set(validProfile(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    docRef.update({
      purrs: 250,
      totalPurrsEarned: 250,
      totalPets: 60,
      selectedBackgroundId: "thanksgiving_feast_hall",
      unlockedBackgrounds: ["cozy_parlor", "thanksgiving_feast_hall"],
      statsPublic: false,
      bio: "Updated bio",
      updatedAt: pastTimestamp(),
    })
  );
  await assertSucceeds(docRef.delete());
});

test("Authenticated user cannot read or write another user's private profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ purrs: 999999, updatedAt: pastTimestamp() }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).delete());
});

test("Rejects shadow ghost fields and invalid types on UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, { isAdmin: true }))
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, { purrs: -5 }))
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, { userId: BOB_UID }))
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, { bio: "x".repeat(200) }))
  );
});

test("Rejects decreasing lifetime totalPurrsEarned on UserProfile update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(docRef.set(validProfile(ALICE_UID, { totalPurrsEarned: 500 })));
  await assertFails(
    docRef.update({ totalPurrsEarned: 100, updatedAt: pastTimestamp() })
  );
});

test("Leaderboard: owner can write & delete, any signed-in user can list, others cannot tamper", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboard(ALICE_UID)));
  await assertSucceeds(bobDb.collection("leaderboard").orderBy("totalPurrsEarned", "desc").limit(20).get());
  await assertFails(
    bobDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboard(ALICE_UID))
  );
  await assertFails(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboard(ALICE_UID, { ghost: "bad" }))
  );
  await assertSucceeds(aliceDb.collection("leaderboard").doc(ALICE_UID).delete());
});

test("Moderation: only verified admin email can write or list; target owner can read only own record", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const spoofAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Verified admin succeeds
  await assertSucceeds(adminDb.collection("moderation").doc(ALICE_UID).set(validModeration(ALICE_UID)));
  await assertSucceeds(adminDb.collection("moderation").limit(20).get());

  // Spoofed unverified admin & regular users are denied writes and collection listing
  await assertFails(spoofAdminDb.collection("moderation").doc(BOB_UID).set(validModeration(BOB_UID)));
  await assertFails(aliceDb.collection("moderation").doc(ALICE_UID).set(validModeration(ALICE_UID)));
  await assertFails(aliceDb.collection("moderation").limit(20).get());

  // Alice can read her own moderation status, Bob cannot
  await assertSucceeds(aliceDb.collection("moderation").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("moderation").doc(ALICE_UID).get());

  // Verified admin can update to TIMED_OUT or BANNED and clear
  await assertSucceeds(
    adminDb.collection("moderation").doc(ALICE_UID).update({
      status: "TIMED_OUT",
      timeoutUntilMillis: Date.now() + 300000,
      updatedByEmail: ADMIN_EMAIL,
      updatedAt: pastTimestamp(),
    })
  );
  await assertSucceeds(adminDb.collection("moderation").doc(ALICE_UID).delete());
});

test("Global announcements: verified admin can create and delete, immutable updates, signed-in can list", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertFails(spoofDb.collection("global_messages").doc("msg_1").set(validAnnouncement("msg_1")));
  await assertFails(aliceDb.collection("global_messages").doc("msg_1").set(validAnnouncement("msg_1")));

  await assertSucceeds(adminDb.collection("global_messages").doc("msg_1").set(validAnnouncement("msg_1")));
  await assertSucceeds(aliceDb.collection("global_messages").orderBy("createdAt", "desc").limit(10).get());

  // Immutable update blocked even for admin
  await assertFails(adminDb.collection("global_messages").doc("msg_1").update({ title: "Edited" }));

  // Alice cannot delete, admin can delete
  await assertFails(aliceDb.collection("global_messages").doc("msg_1").delete());
  await assertSucceeds(adminDb.collection("global_messages").doc("msg_1").delete());
});

test("Limited item Exist counters: signed-in users create at 1 and increment +1 only, cannot forge jumps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  const itemRefAlice = aliceDb.collection("limited_item_stats").doc("pat_golden_touch_cat");
  const itemRefBob = bobDb.collection("limited_item_stats").doc("pat_golden_touch_cat");
  const itemRefAdmin = adminDb.collection("limited_item_stats").doc("pat_golden_touch_cat");

  // Cannot create starting at 50 as regular player
  await assertFails(
    aliceDb.collection("limited_item_stats").doc("pat_bad").set(validCounter("pat_bad", { existCount: 50 }))
  );

  // Create at 1 succeeds, Bob increments to 2
  await assertSucceeds(itemRefAlice.set(validCounter("pat_golden_touch_cat")));
  await assertSucceeds(itemRefBob.update({ existCount: 2, updatedAt: pastTimestamp() }));

  // Regular player cannot jump by +5 or decrement or inject ghost field
  await assertFails(itemRefAlice.update({ existCount: 7, updatedAt: pastTimestamp() }));
  await assertFails(itemRefAlice.update({ existCount: 1, updatedAt: pastTimestamp() }));
  await assertFails(itemRefAlice.update({ existCount: 3, ghost: 1, updatedAt: pastTimestamp() }));

  // Signed-in players can list counters; admin can delete
  await assertSucceeds(aliceDb.collection("limited_item_stats").limit(50).get());
  await assertFails(aliceDb.collection("limited_item_stats").doc("pat_golden_touch_cat").delete());
  await assertSucceeds(itemRefAdmin.delete());

  // BACKGROUND counter type also creates at 1 and increments +1 cleanly
  const bgRefAlice = aliceDb.collection("limited_item_stats").doc("bg_thanksgiving_feast_hall");
  const bgRefBob = bobDb.collection("limited_item_stats").doc("bg_thanksgiving_feast_hall");
  await assertSucceeds(bgRefAlice.set(validCounter("bg_thanksgiving_feast_hall", { itemType: "BACKGROUND" })));
  await assertSucceeds(bgRefBob.update({ existCount: 2, updatedAt: pastTimestamp() }));
});
