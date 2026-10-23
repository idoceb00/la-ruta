import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Community, CommunityRequest, JoinCommunityRequest } from './community.model';

@Service()
export class CommunityService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/api/communities`;

    getMyCommunities(): Observable<Community[]> {
        return this.http.get<Community[]>(this.apiUrl);
    }

    getCommunity(id: number): Observable<Community> {
        return this.http.get<Community>(`${this.apiUrl}/${id}`);
    }

    createCommunity(request: CommunityRequest): Observable<Community> {
        return this.http.post<Community>(this.apiUrl, request);
    }

    joinCommunity(request: JoinCommunityRequest): Observable<Community> {
        return this.http.post<Community>(`${this.apiUrl}/join`, request);
    }

    leaveCommunity(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}/members/me`);
    }
}
