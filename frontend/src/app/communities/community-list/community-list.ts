import { Component, inject } from '@angular/core';
import { CommunityService } from '../community-service';
import { rxResource } from '@angular/core/rxjs-interop';

@Component({
  imports: [],
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

