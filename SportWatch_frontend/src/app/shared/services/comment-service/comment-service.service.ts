import { Injectable } from '@angular/core';
import { BaseCsrfService } from '../base-csrf-service/base-csrf-service.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Comment } from '../../interfaces/Comment';

@Injectable({
    providedIn: 'root'
})
export class CommentService {
    protected readonly apiUrl : string = 'http://localhost:4200/api';
    constructor(private http: HttpClient, private csrf: BaseCsrfService) {}

    /**
      * POST /api/comment
      * Creates a new comment (user must be author).
      */
    createComment(comment: Comment): Observable<{ msg: string }> {
        return this.http.post<{ msg: string }>(`${this.apiUrl}/comment`, comment, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });
    }

    /**
    * GET /api/comment/{stream_id}
    * Fetches all comments for a given stream.
    */
    listStreamComments(streamId: number): Observable<Comment[]> {
        return this.http.get<Comment[]>(`${this.apiUrl}/comment/${streamId}`, {
            withCredentials: true,
            responseType: 'json',
        });
    }

    /**
    * PUT /api/comment/{comment_id}
    * Updates the message of a comment (only if user is author).
    */
    updateCommentMsg(commentId: number, message: string): Observable<{ msg: string }> {
        return this.http.put<{ msg: string }>(`${this.apiUrl}/comment/${commentId}`, message, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken(), 'Content-Type': 'text/plain' }),
            withCredentials: true,
            responseType: 'json',
        });
    }

    /**
    * DELETE /api/comment/{comment_id}
    * Deletes a comment (only if user is author).
    */
    deleteComment(commentId: number): Observable<{ msg: string }> {
        return this.http.delete<{ msg: string }>(`${this.apiUrl}/comment/${commentId}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });
    }
}
