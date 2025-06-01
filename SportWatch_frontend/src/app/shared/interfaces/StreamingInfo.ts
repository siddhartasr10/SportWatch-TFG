export interface StreamingInfo {
  title: string;
  category: string;
  streamUrl: string;     // Using string for URL
  thumbnailUrl: string;  // Using string for URL
  authorId: number;
  desc: string;
  createdAt: string;     // ISO date string (LocalDateTime -> string)
}
