import { Component, inject } from '@angular/core';
import { CommunityService } from '../community-service';
import { rxResource } from '@angular/core/rxjs-interop';
import { CommunityCreateForm } from '../community-create-form/community-create-form';
import { CommunityJoinForm } from '../community-join-form/community-join-form';
import { RouterLink } from '@angular/router';

@Component({
  imports: [CommunityCreateForm, CommunityJoinForm, RouterLink],
  selector: 'app-community-list',
  styleUrl: './community-list.css',
  templateUrl: './community-list.html',
})
export class CommunityList {
  private readonly communityService = inject(CommunityService);

  protected readonly communities = rxResource({
    stream: () => this.communityService.getMyCommunities(),
  });
}

