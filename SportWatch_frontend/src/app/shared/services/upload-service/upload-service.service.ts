import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { IvsChannelInfo } from '../../interfaces/IvsChannelInfo';
import { BaseCsrfService } from '../base-csrf-service/base-csrf-service.service';

@Injectable({providedIn: 'root'})
export class UploadService extends BaseCsrfService {
  constructor(http: HttpClient) {
      super(http);
  }

  /**
   * POST /api/request-channel
   * Request a free or new IVS channel.
   * @param data Object with required 'title' and optional 'description' and 'category'
   */
    requestChannel(data: { title: string, description?: string | null, category?: string | null }): Observable<IvsChannelInfo> {
      return this.http.post<IvsChannelInfo>(`${this.apiUrl}/request-channel`, data, {
            headers: new HttpHeaders({'Content-Type': 'application/json', 'X-XSRF-TOKEN': this.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });
  }
}
