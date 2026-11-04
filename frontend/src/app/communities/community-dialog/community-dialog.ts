import { afterNextRender, Component, computed, ElementRef, inject, input, output, signal, viewChild } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { CommunityService } from '../community-service';
import { ActiveCommunityService } from '../active-community-service';
import { Loading } from '../../shared/loading/loading';
import { ErrorBox } from '../../shared/error-box/error-box';
import { Button } from '../../shared/button/button';

@Component({
  selector: 'app-community-dialog',
  imports: [Loading, ErrorBox, Button],
  templateUrl: './community-dialog.html',
  styleUrl: './community-dialog.css',
})
export class CommunityDialog {
  private readonly communityService = inject(CommunityService);
  private readonly active = inject(ActiveCommunityService);

  readonly communityId = input.required<number>();

  // Tells the parent the dialog has been closed (button, Escape or backdrop)
  readonly closed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly community = rxResource({
    params: () => this.communityId(),
    stream: ({ params: id }) => this.communityService.getCommunity(id),
  });

  protected readonly isActive = computed(() => this.active.activeCommunity()?.id === this.communityId());

  // Web Share API: available on most phones, rarely on desktop
  protected readonly canShare = typeof navigator !== 'undefined' && 'share' in navigator;

  protected readonly copied = signal(false);
  protected readonly confirmingLeave = signal(false);
  protected readonly leaving = signal(false);
  protected readonly leaveError = signal<string | null>(null);

  constructor() {
    // Open as a modal once the <dialog> element exists in the DOM
    afterNextRender(() => this.dialog().nativeElement.showModal());
  }

  protected close(): void {
    this.dialog().nativeElement.close();
  }

  protected onDialogClick(event: MouseEvent): void {
    // The dialog itself is only the target when clicking on the backdrop
    if (event.target === this.dialog().nativeElement) {
      this.close();
    }
  }

  protected useCommunity(): void {
    this.active.select(this.communityId());
    this.close();
  }

  protected async copyInviteCode(code: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(code);
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    } catch {
      // Clipboard API unavailable; the code is still selectable in the UI
    }
  }

  protected async shareInviteCode(name: string, code: string): Promise<void> {
    try {
      await navigator.share({
        title: 'La Ruta',
        text: `Únete a «${name}» en La Ruta con el código ${code}`,
        url: location.origin,
      });
    } catch {
      // The user closed the share sheet; nothing to do
    }
  }

  protected leave(): void {
    if (this.leaving()) {
      return;
    }

    this.leaving.set(true);
    this.leaveError.set(null);

    this.communityService.leaveCommunity(this.communityId()).subscribe({
      next: () => {
        this.active.reload();
        this.close();
      },
      error: () => {
        this.leaveError.set('No se ha podido salir de la comunidad. Inténtalo de nuevo.');
        this.leaving.set(false);
      },
    });
  }
}