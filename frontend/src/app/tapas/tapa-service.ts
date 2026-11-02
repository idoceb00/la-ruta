import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { BarTapa, BarTapaRequest, Tapa, TapaReviewRequest } from './tapa.model';

@Service()
export class TapaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api`;

  searchTapas(query: string): Observable<Tapa[]> {
    let params = new HttpParams();
    const trimmed = query.trim();
    if (trimmed) {
      params = params.set('query', trimmed);
    }
    return this.http.get<Tapa[]>(`${this.apiUrl}/tapas`, { params });
  }

  addTapaToBar(barId: number, request: BarTapaRequest): Observable<BarTapa> {
    return this.http.post<BarTapa>(`${this.apiUrl}/bars/${barId}/tapas`, request);
  }

  removeTapaFromBar(barId: number, tapaId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bars/${barId}/tapas/${tapaId}`);
  }

  saveMyTapaReview(barId: number, tapaId: number, request: TapaReviewRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/bars/${barId}/tapas/${tapaId}/review`, request);
  }

  deleteMyTapaReview(barId: number, tapaId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bars/${barId}/tapas/${tapaId}/review`);
  }
}