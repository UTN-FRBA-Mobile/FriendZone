export function sortedUserPair(
  userIdA: string,
  userIdB: string,
): { userIdLow: string; userIdHigh: string } {
  if (userIdA < userIdB) {
    return { userIdLow: userIdA, userIdHigh: userIdB };
  }
  return { userIdLow: userIdB, userIdHigh: userIdA };
}
