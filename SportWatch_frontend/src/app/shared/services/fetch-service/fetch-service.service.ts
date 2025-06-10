import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map, Observable } from 'rxjs';

import { StreamingInfo } from '../../interfaces/StreamingInfo';
import { BaseCsrfService } from '../base-csrf-service/base-csrf-service.service';

@Injectable({
  providedIn: 'root',
})
export class FetchService extends BaseCsrfService {
  constructor(http: HttpClient) {
      super(http);
      this.INVALIDURL = "https://google.com";
  }
    // IF ANY URL IS INVALID, IT WILL RETURN "https://google.com".
  INVALIDURL : string;

  // GET /api/streams - returns both uploaded and non-uploaded streams concatenated
  fetchAllStreams(): Observable<StreamingInfo[]> {
      return this.http.get<StreamingInfo[]>(`${this.apiUrl}/streams`)
          .pipe(map(this.validUrlFilter)) as Observable<StreamingInfo[]>;

  }
 // POST /api/streams - returns both uploaded and non-uploaded streams concatenated
  fetchAllStreamsCustomDuration(urlDuration: number): Observable<StreamingInfo[]> {
    const body = { urlDuration: urlDuration.toString() };
    return this.http.post<StreamingInfo[]>(`${this.apiUrl}/streams`, body, {
      headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.getXsrfToken() }),
      withCredentials: true,
      responseType: 'json',
    }).pipe(map(this.validUrlFilter)) as Observable<StreamingInfo[]>;
  }


  // GET /api/stream/{streamId} - fetch stream info by id without duration
  fetchStreamById(streamId: number): Observable<StreamingInfo> {
        return this.http.get<StreamingInfo>(`${this.apiUrl}/stream/${streamId}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        }).pipe(map(this.validUrlFilter)) as Observable<StreamingInfo>;
    }
    // POST /api/stream/{stream_id} - fetch stream info by id with optional urlDuration in hours
  fetchStreamByIdCustomDuration(streamId: number, urlDuration?: number): Observable<StreamingInfo> {
      const body = urlDuration ? { urlDuration: urlDuration.toString() } : {};
      return this.http.post<StreamingInfo>(`${this.apiUrl}/stream/${streamId}`, body, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
      }).pipe(map(this.validUrlFilter)) as Observable<StreamingInfo>;
  }

    // I need to cast bc function can return both types, but only returns the type that gets passed to it.
    // if you passed one obj it will return one.

    // El indice lo pasa map tambien, no se que es pero lo pasa a la funcion.
    // La funcion: (project: (value: StreamingInfo[], index: number) => StreamingInfo[])

    // I did it a closure bc for some very strange reason 'this' context isn't saving inside the rxjs map function.
  private validUrlFilter = (streams: StreamingInfo[] | StreamingInfo, index: number) : StreamingInfo[] | StreamingInfo => {
      let isStreamsList = (streams as StreamingInfo).author === undefined;

      if (!isStreamsList) streams = [streams as StreamingInfo];
      else streams = streams as StreamingInfo[];

      let filteredStreams = streams.map((stream: StreamingInfo) => {
              stream.streamUrl = (stream.streamUrl === this.INVALIDURL)
                  ? stream.streamUrl = "" : stream.streamUrl;

              stream.thumbnailUrl= (stream.thumbnailUrl === this.INVALIDURL)
                  ? stream.thumbnailUrl = "" : stream.thumbnailUrl;

              return stream;
          });

      // I only want to return a list when a list was list was entered
      // bakend only emits one value for fetchbyId.
      // and when i fetchById I want to see an obj not an array.
      return (!isStreamsList) ? filteredStreams[0] : filteredStreams;

  }
}
