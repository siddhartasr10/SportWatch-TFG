export interface StreamingInfo {
  streamId: number;
  isLive: boolean;
  title: string;
  category: string;
  streamUrl: string;     // Using string for URL
  thumbnailUrl: string;  // Using string for URL
  author: string;
  authorId: number;
  desc: string;
  createdAt: string;     // ISO date string (LocalDateTime -> string)
}
