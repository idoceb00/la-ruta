import { Component, inject, input, numberAttribute, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { CommunityService } from '../community-service';

@Component({
  selector: 'app-community-detail',
  imports: [RouterLink],
  templateUrl: './community-detail.html',
  styleUrl: './community-detail.css',
})
export class CommunityDetail {
  private readonly communityService = inject(CommunityService);

  readonly id = input.required({ transform: numberAttribute });

  protected readonly community = rxResource({
    params: () => this.id(),
    stream: ({ params: id }) => this.communityService.getCommunity(id),
  });

  protected readonly copied = signal(false);

  protected async copyInviteCode(code: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(code);
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    } catch {
      // Clipboard API unavailable; the code is still selectable in the UI
    }
  }

  private readonly router = inject(Router);

  protected readonly confirmingLeave = signal(false);
  protected readonly leaving = signal(false);
  protected readonly leaveError = signal<string | null>(null);

  protected leave(): void {
    if (this.leaving()) {
      return;
    }

    this.leaving.set(true);
    this.leaveError.set(null);

    this.communityService.leaveCommunity(this.id()).subscribe({
      next: () => {
        void this.router.navigate(['/communities']);
      },
      error: () => {
        this.leaveError.set('No se ha podido salir de la comunidad. Inténtalo de nuevo.');
        this.leaving.set(false);
      },
    });
  }
}