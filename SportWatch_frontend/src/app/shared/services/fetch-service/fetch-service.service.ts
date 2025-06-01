import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { StreamingInfo } from '../../interfaces/StreamingInfo';
import { BaseCsrfService } from '../base-csrf-service/base-csrf-service.service';

@Injectable({
  providedIn: 'root',
})
export class FetchService extends BaseCsrfService {
  constructor(http: HttpClient) {
    super(http);
  }

  // GET /api/streams - returns both uploaded and non-uploaded streams concatenated
  fetchAllStreams(): Observable<StreamingInfo[]> {
      return this.http.get<StreamingInfo[]>(`${this.apiUrl}/streams`);
  }

  // POST /api/stream/{stream_id} - fetch stream info by id with optional urlDuration in hours
  fetchStreamById(streamId: number, urlDuration?: number): Observable<StreamingInfo> {
    const body = urlDuration ? { urlDuration: urlDuration.toString() } : {};
    return this.http.post<StreamingInfo>(`${this.apiUrl}stream/${streamId}`, body);
  }
}
