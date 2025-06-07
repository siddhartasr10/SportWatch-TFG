export interface ExtUser {
  userId: number;
  username: string;
  password: string;
  email: string;
  created_at: string; // ISO string representation of LocalDateTime
  description: string;
  notifications: string[] | null; // each string represents a char[128]
  authorities: string[]; // Spring Security authorities, only can be "USER"
}
