import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { rxResource, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { ActiveCommunityService } from '../../communities/active-community-service';
import { BarCard } from '../bar-card/bar-card';
import { Bar } from '../bar.model';
import { BarService } from '../bar-service';
import { Loading } from '../../shared/loading/loading';
import { ErrorBox } from '../../shared/error-box/error-box';

@Component({
  selector: 'app-bar-list',
  imports: [BarCard, RouterLink, Loading, ErrorBox],
  templateUrl: './bar-list.html',
  styleUrl: './bar-list.css',
})
export class BarList {
  private readonly barService = inject(BarService);
  protected readonly active = inject(ActiveCommunityService);

  // What the user types, updated on every keystroke
  protected readonly query = signal('');

  // What is actually sent to the API, once the user stops typing
  private readonly debouncedQuery = toSignal(
    toObservable(this.query).pipe(
      map((q) => q.trim()),
      debounceTime(300),
      distinctUntilChanged(),
    ),
    { initialValue: '' },
  );

  private readonly communityId = computed(() => this.active.activeCommunity()?.id);

  protected readonly bars = rxResource({
    params: () => {
      const communityId = this.communityId();
      return communityId === undefined ? undefined : { communityId, query: this.debouncedQuery() };
    },
    stream: ({ params }) => this.barService.getBars(params.communityId, params.query),
  });

  // Keeps the last results visible while a new search is loading
  protected readonly shownBars = linkedSignal<Bar[] | undefined, Bar[] | undefined>({
    source: this.bars.value,
    computation: (bars, previous) => bars ?? previous?.value,
  });

  protected readonly barCount = computed(() => this.shownBars()?.length ?? 0);
  protected readonly searching = computed(() => this.debouncedQuery() !== '');

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