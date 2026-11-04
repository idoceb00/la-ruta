import { Component, computed, input } from '@angular/core';

interface Part {
  text: string;
  match: boolean;
}

// Lowercase and strip accents char by char, keeping the same length as the original,
// so positions found in the folded text are valid in the original one
function fold(text: string): string {
  return Array.from(text, (ch) => {
    const folded = ch.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase();
    return folded.length === ch.length ? folded : ch;
  }).join('');
}

@Component({
  selector: 'app-highlight-text',
  template: `@for (part of parts(); track $index) {@if (part.match) {<mark class="rounded-xs bg-mustard text-ink">{{ part.text }}</mark>} @else {<span>{{ part.text }}</span>}}`,
})
export class HighlightText {
  readonly text = input.required<string>();
  readonly query = input('');

  protected readonly parts = computed<Part[]>(() => {
    const text = this.text();
    const needle = fold(this.query().trim());
    if (!needle) {
      return [{ text, match: false }];
    }

    const haystack = fold(text);
    const parts: Part[] = [];
    let from = 0;
    let index = haystack.indexOf(needle, from);

    while (index !== -1) {
      if (index > from) {
        parts.push({ text: text.slice(from, index), match: false });
      }
      parts.push({ text: text.slice(index, index + needle.length), match: true });
      from = index + needle.length;
      index = haystack.indexOf(needle, from);
    }

    if (from < text.length) {
      parts.push({ text: text.slice(from), match: false });
    }
    return parts;
  });
}