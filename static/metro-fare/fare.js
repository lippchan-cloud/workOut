/** Fare brackets from Shanghai metro hearing schemes (2026-08). */

export function currentFare(km) {
  if (!(km > 0)) return null;
  if (km <= 6) return 3;
  return 3 + Math.ceil((km - 6) / 10);
}

/** 方案一：起乘 0–4km / 3元；后续 4×3、7×3、10×3；67km 以上每 15km +1 */
export function scheme1Fare(km) {
  if (!(km > 0)) return null;
  if (km <= 4) return 3;
  const steps = [4, 4, 4, 7, 7, 7, 10, 10, 10];
  let bound = 4;
  let price = 3;
  for (const step of steps) {
    bound += step;
    price += 1;
    if (km <= bound) return price;
  }
  return 12 + Math.ceil((km - 67) / 15);
}

/** 方案二：起乘 0–6km / 4元；后续 6,8,8,10,10,12,12；72km 以上每 14km +1 */
export function scheme2Fare(km) {
  if (!(km > 0)) return null;
  if (km <= 6) return 4;
  const steps = [6, 8, 8, 10, 10, 12, 12];
  let bound = 6;
  let price = 4;
  for (const step of steps) {
    bound += step;
    price += 1;
    if (km <= bound) return price;
  }
  return 11 + Math.ceil((km - 72) / 14);
}

export function lookupFares(km) {
  const current = currentFare(km);
  const scheme1 = scheme1Fare(km);
  const scheme2 = scheme2Fare(km);
  if (current == null) return null;
  return {
    km,
    current,
    scheme1,
    scheme2,
    delta1: scheme1 - current,
    delta2: scheme2 - current,
    beyondTable: km > 124,
  };
}
