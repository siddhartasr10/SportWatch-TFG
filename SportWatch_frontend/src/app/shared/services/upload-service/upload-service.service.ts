import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
   * @param data Object with required 'title' and optional 'desc' and 'category'
   */
  requestChannel(data: { title: string; desc?: string; category?: string }): Observable<IvsChannelInfo> {
    return this.http.post<IvsChannelInfo>(`${this.apiUrl}/request-channel`, data);
  }
}
