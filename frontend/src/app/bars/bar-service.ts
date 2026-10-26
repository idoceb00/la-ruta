import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Bar, BarRequest } from './bar.model';

@Service()
export class BarService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api`;

  getBars(communityId: number, query?: string): Observable<Bar[]> {
    let params = new HttpParams();
    const trimmed = query?.trim();
    if (trimmed) {
      params = params.set('query', trimmed);
    }
    return this.http.get<Bar[]>(`${this.apiUrl}/communities/${communityId}/bars`, { params });
  }

  getBar(id: number): Observable<Bar> {
    return this.http.get<Bar>(`${this.apiUrl}/bars/${id}`);
  }

  createBar(communityId: number, request: BarRequest): Observable<Bar> {
    return this.http.post<Bar>(`${this.apiUrl}/communities/${communityId}/bars`, request);
  }

  updateBar(id: number, request: BarRequest): Observable<Bar> {
    return this.http.put<Bar>(`${this.apiUrl}/bars/${id}`, request);
  }

  deleteBar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bars/${id}`);
  }
}