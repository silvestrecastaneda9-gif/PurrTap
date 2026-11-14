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
    unlockedPatterns: ["tabby"],
    unlockedAccessories: ["none"],
    upgradeLevels: { feather_wand: 1 },
    dailyStreak: 1,
    lastDailyClaimEpochDay: 20000,
    claimedQuestIds: [],
    unlockedBadgeIds: ["first_purr"],
    equippedBadgeId: "first_purr",
    eventPurrsEarned: 0,
    preferredCity: "San Francisco",
    weatherCheckedCount: 1,
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

test("Unauthenticated user cannot read or write users or leaderboard", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("leaderboard").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
});

test("Authenticated user can create, read, and update own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(docRef.set(validProfile(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    docRef.update({
      purrs: 250,
      totalPurrsEarned: 250,
      totalPets: 60,
      updatedAt: pastTimestamp(),
    })
  );
});

test("Authenticated user cannot read or write another user's private profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ purrs: 999999, updatedAt: pastTimestamp() }));
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
});

test("Rejects decreasing lifetime totalPurrsEarned on UserProfile update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(docRef.set(validProfile(ALICE_UID, { totalPurrsEarned: 500 })));
  await assertFails(
    docRef.update({ totalPurrsEarned: 100, updatedAt: pastTimestamp() })
  );
});

test("Leaderboard: owner can write, any signed-in user can list, others cannot tamper", async () => {
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
});
