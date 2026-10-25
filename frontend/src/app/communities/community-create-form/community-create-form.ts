import { Component, inject, output, signal } from '@angular/core';
import { form, FormField, maxLength, required } from '@angular/forms/signals';
import { Community, CommunityRequest } from '../community.model';
import { CommunityService } from '../community-service';

@Component({
  selector: 'app-community-create-form',
  imports: [FormField],
  templateUrl: './community-create-form.html',
  styleUrl: './community-create-form.css',
})
export class CommunityCreateForm {
  private readonly communityService = inject(CommunityService);

  readonly created = output<Community>();

  private readonly model = signal<CommunityRequest>({ name: '' });

  protected readonly createForm = form(this.model, (path) => {
    required(path.name, { message: 'El nombre es obligatorio' });
    maxLength(path.name, 100, { message: 'Máximo 100 caracteres' });
  });

  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected onSubmit(event: Event): void {
    event.preventDefault();
    if (this.createForm().invalid() || this.submitting()) {
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    this.communityService.createCommunity({ name: this.model().name.trim() }).subscribe({
      next: (community) => {
        this.model.set({ name: '' });
        this.createForm().reset();
        this.submitting.set(false);
        this.created.emit(community);
      },
      error: () => {
        this.serverError.set('No se ha podido crear la comunidad. Inténtalo de nuevo.');
        this.submitting.set(false);
      },
    });
  }
}