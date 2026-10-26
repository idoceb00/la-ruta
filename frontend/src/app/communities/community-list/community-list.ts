import { Component, inject } from '@angular/core';
import { CommunityCreateForm } from '../community-create-form/community-create-form';
import { CommunityJoinForm } from '../community-join-form/community-join-form';
import { RouterLink } from '@angular/router';
import { ActiveCommunityService } from '../active-community-service';
import { Community } from '../community.model';
import { Loading } from '../../shared/loading/loading';
import { ErrorBox } from '../../shared/error-box/error-box';

@Component({
  imports: [CommunityCreateForm, CommunityJoinForm, RouterLink, Loading, ErrorBox],
  selector: 'app-community-list',
  styleUrl: './community-list.css',
  templateUrl: './community-list.html',
})
export class CommunityList {
  private readonly active = inject(ActiveCommunityService);
  
  protected readonly communities = this.active.communities;

  protected onCommunityAdded(community: Community): void {
    this.active.select(community.id);
    this.active.reload();
  }

}

