import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { rxResource, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { debounce, distinctUntilChanged, map, timer } from 'rxjs';
import { ActiveCommunityService } from '../../communities/active-community-service';
import { BarCard } from '../bar-card/bar-card';
import { Bar } from '../bar.model';
import { BarService } from '../bar-service';
import { Loading } from '../../shared/loading/loading';
import { ErrorBox } from '../../shared/error-box/error-box';
import { Button } from '../../shared/button/button';

@Component({
  selector: 'app-bar-list',
  imports: [BarCard, RouterLink, Loading, ErrorBox, Button],
  templateUrl: './bar-list.html',
  styleUrl: './bar-list.css',
})
export class BarList {
  private readonly barService = inject(BarService);
  protected readonly active = inject(ActiveCommunityService);

  // What the user types, updated on every keystroke
  protected readonly query = signal('');

  // What is actually sent to the API: waits while typing, but clears instantly
  protected readonly debouncedQuery = toSignal(
    toObservable(this.query).pipe(
      map((q) => q.trim()),
      debounce((q) => timer(q ? 300 : 0)),
      distinctUntilChanged(),
    ),
    { initialValue: '' },
  );

  protected readonly searching = computed(() => this.debouncedQuery() !== '');

  private readonly communityId = computed(() => this.active.activeCommunity()?.id);

  // Only fetches while there is something to search for
  protected readonly bars = rxResource({
    params: () => {
      const communityId = this.communityId();
      const query = this.debouncedQuery();
      return communityId === undefined || query === '' ? undefined : { communityId, query };
    },
    stream: ({ params }) => this.barService.getBars(params.communityId, params.query),
  });

  // Keeps the last results visible while a new search loads, and empties when the search is cleared
  protected readonly shownBars = linkedSignal<
    { bars: Bar[] | undefined; searching: boolean },
    Bar[] | undefined
  >({
    source: () => ({ bars: this.bars.value(), searching: this.searching() }),
    computation: (source, previous) =>
      source.searching ? (source.bars ?? previous?.value) : undefined,
  });

  protected readonly barCount = computed(() => this.shownBars()?.length ?? 0);

  protected onSearch(event: Event): void {
    this.query.set((event.target as HTMLInputElement).value);
  }

  protected clearSearch(): void {
    this.query.set('');
  }

  protected selectCommunity(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    this.active.select(id);
  }
}