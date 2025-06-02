export interface StreamingInfo {
  streamId: number;
  title: string;
  category: string;
  streamUrl: string;     // Using string for URL
  thumbnailUrl: string;  // Using string for URL
  authorId: number;
  author: string;
  desc: string;
  createdAt: string;     // ISO date string (LocalDateTime -> string)
  isLive: boolean;
}
