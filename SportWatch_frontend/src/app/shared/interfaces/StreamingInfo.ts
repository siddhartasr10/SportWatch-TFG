export interface StreamingInfo {
  author: string;
  authorId: number;
  streamId: number;
  isLive: boolean;
  title: string;
  category: string;
  streamUrl: string;     // Using string for URL
  thumbnailUrl: string;  // Using string for URL
  desc: string;
  createdAt: string;     // ISO date string (LocalDateTime -> string)
  viewerCount?: number; // Property set on the frontend.
}
