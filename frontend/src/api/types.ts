export interface ConflictFlag {
  conflicting_booking_id: string;
  conflict_type: 'location' | 'resource';
}

export interface CustomFieldValue {
  field_id: string;
  value: string;
}

export interface Booking {
  id: string;
  series_id: string | null;
  location_id: string;
  resource_ids: string[];
  title: string;
  description: string | null;
  start_time: string; // ISO 8601
  end_time: string;
  status: 'pending' | 'approved' | 'denied';
  owner_id: string;
  conflict_flags: ConflictFlag[];
  custom_field_values: CustomFieldValue[];
  created_at: string;
  updated_at: string;
  deleted_at: string | null;
}

export interface Location {
  id: string;
  name: string;
  colour: string; // CSS class or hex colour
  building: string | null;
  layouts: string | null;
  deleted_at: string | null;
}

export interface Resource {
  id: string;
  name: string;
  type: string | null;
  description: string | null;
  is_unique: boolean;
  restrict_locations: string[] | null; // location_id[]
  deleted_at: string | null;
}

export interface Settings {
  id: number; // always 1, singleton
  approve_booking: boolean;
  calendar_slot_size: number;
  calendar_min_time: string; // "08:00"
  calendar_max_time: string; // "18:00"
}

export interface CustomField {
  id: string;
  name: string;
  field_type: 'textfield' | 'select' | 'textarea' | 'radio' | 'checkbox';
  required: boolean;
  options: string[] | null;
  context_id: string | null; // null = global, otherwise specific Location id
}

export interface User {
  id: string; // Keycloak sub (UUID)
  email: string;
  first_name: string;
  last_name: string;
  deleted_at: string | null;
}

export interface Permission {
  user_id: string;
  allow_login: boolean;
  allow_booking: boolean;
  allow_approve_booking: boolean;
  allow_manage_locations: boolean;
  allow_manage_resources: boolean;
  allow_manage_users: boolean;
  allow_manage_settings: boolean;
  allow_calendar_viewer: boolean;
  allow_audit_viewer: boolean;
  [key: string]: boolean | string;
}

export interface FeedBooking {
  id: string;
  title: string;
  location_id: string;
  location_name: string;
  start_time: string;
  end_time: string;
  status: 'approved';
}
