const fs = require("fs");
const path = require("path");

const root = process.cwd();
const failures = [];

function read(relPath) {
  return fs.readFileSync(path.join(root, relPath), "utf8");
}

function requireText(file, text, message) {
  if (!read(file).includes(text)) failures.push(`${file}: ${message}`);
}

function requireRegex(file, regex, message) {
  if (!regex.test(read(file))) failures.push(`${file}: ${message}`);
}

for (const helper of [
  "validTakSchoolCreate",
  "validTakProfileCreate",
  "takSchoolOwnedAfter",
  "validTakStudent",
  "validTakTask",
  "validTakNotice",
  "validTakSchedule",
]) {
  requireText("firestore.rules", `function ${helper}`, `missing ${helper} guard`);
}

requireText(
  "firestore.rules",
  "getAfter(/databases/$(database)/documents/takstud_schools/$(schoolId)).data.owner_id == request.auth.uid",
  "profile creation must prove ownership of the target school with getAfter",
);
requireRegex(
  "firestore.rules",
  /allow\s+create:\s+if\s+signedIn\(\)\s+&&\s+validTakProfileCreate\(userId,\s+request\.resource\.data\);/m,
  "takstud_profiles create must use validTakProfileCreate",
);
requireRegex(
  "firestore.rules",
  /allow\s+delete:\s+if\s+false;\s*\n\s*}\s*\n\s*match\s+\/takstud_profiles/m,
  "takstud_schools delete must stay blocked from the browser",
);

for (const token of [
  "CACHE_KEYS_BY_COLLECTION",
  "DATA_LIMITS",
  "cacheRecords(cacheKey, data)",
  "queueRealtimeRender(cacheKey)",
  "invalidateCache('students')",
  "invalidateCache('tasks')",
  "invalidateCache('notices')",
  "invalidateCache('schedules')",
]) {
  requireText("script.js", token, `missing production data-flow token ${token}`);
}

requireRegex(
  "script.js",
  /onSnapshot\(\s*q,\s*\n\s*snapshot\s+=>\s+callback\(\{/m,
  "Firestore realtime must consume snapshot data directly",
);
requireRegex(
  "script.js",
  /\.order\('created_at',\s*\{\s*ascending:\s*false\s*\}\)\.limit\(DATA_LIMITS\.tasks\)/m,
  "task reads must stay bounded for production use",
);

if (failures.length) {
  console.error("TAKSTUD_PRODUCTION_CHECK_FAILED");
  for (const failure of failures) console.error(`- ${failure}`);
  process.exit(1);
}

console.log("TAKSTUD_PRODUCTION_CHECK_OK");
