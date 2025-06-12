// Properties that are optional are not needed to create a comment.
export interface Comment {
    commentId?: number;
    authorId?: number;
    streamId: number;
    comment: string;
    createdAt?: string;
    author?: string; // Property set to display on the frontend backend doesn't return it.
}
