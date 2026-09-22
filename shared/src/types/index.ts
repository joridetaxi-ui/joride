export enum UserRole {
  CUSTOMER = 'CUSTOMER',
  CAPTAIN = 'CAPTAIN',
  ADMIN = 'ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN',
  KYC_ADMIN = 'KYC_ADMIN',
  FINANCE_ADMIN = 'FINANCE_ADMIN',
  SUPPORT_ADMIN = 'SUPPORT_ADMIN'
}

export enum ServiceType {
  BIKE_TAXI = 'BIKE_TAXI',
  AUTO = 'AUTO',
  PARCEL = 'PARCEL',
  PRIME_BIKE = 'PRIME_BIKE'
}

export enum KycStatus {
  PENDING = 'PENDING',
  UNDER_REVIEW = 'UNDER_REVIEW',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  SUSPENDED = 'SUSPENDED',
  BLOCKED = 'BLOCKED'
}

export enum RideStatus {
  REQUESTED = 'REQUESTED',
  SEARCHING = 'SEARCHING',
  ACCEPTED = 'ACCEPTED',
  ARRIVED = 'ARRIVED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED_BY_CUSTOMER = 'CANCELLED_BY_CUSTOMER',
  CANCELLED_BY_CAPTAIN = 'CANCELLED_BY_CAPTAIN'
}

export enum ParcelStatus {
  REQUESTED = 'REQUESTED',
  ACCEPTED = 'ACCEPTED',
  ARRIVED_PICKUP = 'ARRIVED_PICKUP',
  PACKAGE_PICKED_UP = 'PACKAGE_PICKED_UP',
  IN_TRANSIT = 'IN_TRANSIT',
  ARRIVED_DESTINATION = 'ARRIVED_DESTINATION',
  DELIVERED = 'DELIVERED',
  RETURNED = 'RETURNED',
  FAILED = 'FAILED'
}

export enum PaymentMethod {
  UPI = 'UPI',
  CASH = 'CASH',
  WALLET = 'WALLET',
  CARD = 'CARD'
}

export interface LocationCoordinates {
  lat: number;
  lng: number;
  address: string;
  title: string;
}

export interface RideRequestPayload {
  customerId: string;
  serviceType: ServiceType;
  pickup: LocationCoordinates;
  drop: LocationCoordinates;
  paymentMethod: PaymentMethod;
  couponCode?: string;
  forWhom?: string;
}

export interface ParcelRequestPayload {
  senderId: string;
  pickup: LocationCoordinates;
  drop: LocationCoordinates;
  receiverName: string;
  receiverPhone: string;
  packageCategory: string;
  packageWeightKg: number;
  instructions?: string;
  paymentMethod: PaymentMethod;
}

export interface DispatchIncomingPayload {
  rideId: string;
  serviceType: ServiceType;
  pickupAddress: string;
  pickupDistanceKm: number;
  dropAddress: string;
  tripDistanceKm: number;
  estimatedEarnings: number;
  paymentMethod: PaymentMethod;
  countdownSeconds: number;
}
