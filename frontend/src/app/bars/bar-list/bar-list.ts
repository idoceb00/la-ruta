import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { ActiveCommunityService } from '../../communities/active-community-service';
import { BarCard } from '../bar-card/bar-card';
import { BarService } from '../bar-service';

@Component({
  selector: 'app-bar-list',
  imports: [BarCard, RouterLink],
  templateUrl: './bar-list.html',
  styleUrl: './bar-list.css',
})
export class BarList {
  private readonly barService = inject(BarService);
  protected readonly active = inject(ActiveCommunityService);

  protected readonly bars = rxResource({
    params: () => this.active.activeCommunity()?.id,
    stream: ({ params: communityId }) => this.barService.getBars(communityId),
  });

  protected readonly barCount = computed(() => this.bars.value()?.length ?? 0);

  protected selectCommunity(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    this.active.select(id);
  }
}