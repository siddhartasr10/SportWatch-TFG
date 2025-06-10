import { Injectable } from '@angular/core';
import { BaseCsrfService } from '../base-csrf-service/base-csrf-service.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StreamService {
    protected readonly apiUrl : string = 'http://localhost:4200/api';
    constructor(private http: HttpClient, private csrf: BaseCsrfService) {}

  /**
   * POST /api/view/{stream_id}
   * Sends a view event for a stream.
   * Requires CSRF token.
   */
    viewStream(streamId: number): Observable<{msg:string}> {
      return this.http.post<{msg:string}>(`${this.apiUrl}/view/${streamId}`, null, {
      headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
      withCredentials: true,
      responseType: 'json',
    });
  }

  /**
   * GET /api/stream/{stream_id}/views
   * Retrieves total view count for a stream.
   */
    // Acabo de acordarme de que las solicitudes get no necesitan ir pasando el token csrf.
    // uups.
  getStreamViews(streamId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stream/${streamId}/views`);
  }
}
