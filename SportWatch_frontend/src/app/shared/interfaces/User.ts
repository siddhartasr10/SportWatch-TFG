export interface ExtUser {
  user_id: number;
  username: string;
  password: string;
  email: string;
  created_at: string; // ISO string representation of LocalDateTime
  notifications: string[] | null; // each string represents a char[128]
  authorities: string[]; // Spring Security authorities, only can be "USER"
}
