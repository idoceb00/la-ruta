import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, output, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { Community, JoinCommunityRequest } from '../community.model';
import { CommunityService } from '../community-service';

@Component({
  selector: 'app-community-join-form',
  imports: [FormField],
  templateUrl: './community-join-form.html',
  styleUrl: './community-join-form.css',
})
export class CommunityJoinForm {
  private readonly communityService = inject(CommunityService);

  readonly joined = output<Community>();

  private readonly model = signal<JoinCommunityRequest>({ inviteCode: '' });

  protected readonly joinForm = form(this.model, (path) => {
    required(path.inviteCode, { message: 'Introduce un código de invitación' });
  });

  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.joinForm().invalid() || this.submitting()) {
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    this.communityService.joinCommunity({ inviteCode: this.model().inviteCode.trim() }).subscribe({
      next: (community) => {
        this.model.set({ inviteCode: '' });
        this.joinForm().reset();
        this.submitting.set(false);
        this.joined.emit(community);
      },
      error: (error: HttpErrorResponse) => {
        this.serverError.set(this.errorMessage(error));
        this.submitting.set(false);
      },
    });
  }

  private errorMessage(error: HttpErrorResponse): string {
    switch (error.status) {
      case 404:
        return 'No existe ninguna comunidad con ese código.';
      case 409:
        return 'Ya perteneces a esa comunidad.';
      default:
        return 'No se ha podido unir a la comunidad. Inténtalo de nuevo.';
    }
  }
}