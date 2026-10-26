import { computed, inject, Service, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { AuthService } from '../auth/auth-service';
import { CommunityService } from './community-service';
import { Community } from './community.model';

const STORAGE_KEY = 'laruta.activeCommunityId';

@Service()
export class ActiveCommunityService {
  private readonly communityService = inject(CommunityService);
  private readonly authService = inject(AuthService);

  // User's communities; only loaded while authenticated
  readonly communities = rxResource({
    params: () => (this.authService.isAuthenticated() ? true : undefined),
    stream: () => this.communityService.getMyCommunities(),
  });

  private readonly selectedId = signal<number | null>(readStoredId());

  // Selected community if it still belongs to the user, otherwise the first one
  readonly activeCommunity = computed<Community | null>(() => {
    const list = this.communities.value() ?? [];
    if (list.length === 0) {
      return null;
    }
    return list.find((c) => c.id === this.selectedId()) ?? list[0];
  });

  select(id: number): void {
    this.selectedId.set(id);
    try {
      localStorage.setItem(STORAGE_KEY, String(id));
    } catch {
      // Storage unavailable; the selection still works for this session
    }
  }

  reload(): void {
    this.communities.reload();
  }
}

function readStoredId(): number | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? Number(raw) : null;
  } catch {
    return null;
  }
}