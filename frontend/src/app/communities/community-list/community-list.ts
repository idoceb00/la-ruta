import { Component, computed, inject, signal } from '@angular/core';
import { CommunityCreateForm } from '../community-create-form/community-create-form';
import { CommunityJoinForm } from '../community-join-form/community-join-form';
import { CommunityDialog } from '../community-dialog/community-dialog';
import { ActiveCommunityService } from '../active-community-service';
import { Community } from '../community.model';
import { Loading } from '../../shared/loading/loading';
import { ErrorBox } from '../../shared/error-box/error-box';
import { Button } from '../../shared/button/button';

type OpenForm = 'join' | 'create' | null;

@Component({
  imports: [CommunityCreateForm, CommunityJoinForm, CommunityDialog, Loading, ErrorBox, Button],
  selector: 'app-community-list',
  styleUrl: './community-list.css',
  templateUrl: './community-list.html',
})
export class CommunityList {
  private readonly active = inject(ActiveCommunityService);

  protected readonly communities = this.active.communities;
  protected readonly activeId = computed(() => this.active.activeCommunity()?.id);

  // Which of the two forms is open; tapping the same button again closes it
  protected readonly openForm = signal<OpenForm>(null);

  // Community shown in the dialog
  protected readonly selectedId = signal<number | null>(null);

  protected toggleForm(form: Exclude<OpenForm, null>): void {
    this.openForm.update((current) => (current === form ? null : form));
  }

  protected onCommunityAdded(community: Community): void {
    this.openForm.set(null);
    this.active.select(community.id);
    this.active.reload();
  }
}