// Trims a text field and turns it into null when nothing is left
export function toNullable(value: string): string | null {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}