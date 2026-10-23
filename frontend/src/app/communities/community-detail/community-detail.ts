import { Component, inject, input, numberAttribute, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
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
}