import express, { Request, Response } from 'express';
import http from 'http';
import { Server, Socket } from 'socket.io';
import cors from 'cors';
import { ServiceType, RideStatus, ParcelStatus, PaymentMethod, KycStatus, UserRole } from '../../shared/src/types';

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE']
  }
});

app.use(cors());
app.use(express.json());

// ==========================================
// IN-MEMORY STORAGE / DATABASE MOCK (Synced with Prisma Schema)
// ==========================================
interface UserRecord {
  id: string;
  phoneNumber: string;
  name: string;
  email?: string;
  role: UserRole;
  walletBalance: number;
  rating: number;
  emergencyContact?: string;
}

interface CaptainRecord {
  id: string;
  userId: string;
  name: string;
  phone: string;
  vehicleType: ServiceType;
  vehicleModel: string;
  vehicleNumber: string;
  kycStatus: KycStatus;
  isOnline: boolean;
  currentLat: number;
  currentLng: number;
  rating: number;
  todayEarnings: number;
  totalEarnings: number;
  walletBalance: number;
  socketId?: string;
}

interface RideRecord {
  id: string;
  customerId: string;
  customerName: string;
  customerPhone: string;
  captainId?: string;
  captainName?: string;
  captainVehicle?: string;
  serviceType: ServiceType;
  status: RideStatus;
  startOtp: string;
  pickup: { lat: number; lng: number; address: string; title: string };
  drop: { lat: number; lng: number; address: string; title: string };
  distanceKm: number;
  durationMins: number;
  totalFare: number;
  platformFee: number;
  captainEarnings: number;
  paymentMethod: PaymentMethod;
  isPaid: boolean;
  couponApplied?: string;
  discount: number;
  createdAt: string;
}

interface ParcelRecord {
  id: string;
  senderId: string;
  senderName: string;
  senderPhone: string;
  captainId?: string;
  receiverName: string;
  receiverPhone: string;
  packageCategory: string;
  packageWeightKg: number;
  pickup: { lat: number; lng: number; address: string };
  drop: { lat: number; lng: number; address: string };
  pickupOtp: string;
  deliveryOtp: string;
  fare: number;
  status: ParcelStatus;
  proofOfDelivery?: string;
  createdAt: string;
}

interface CouponRecord {
  code: string;
  discountPercent?: number;
  fixedDiscount?: number;
  maxDiscount?: number;
  minBookingVal: number;
  serviceType?: ServiceType;
  isActive: boolean;
}

const db = {
  users: new Map<string, UserRecord>([
    ['CUST-001', { id: 'CUST-001', phoneNumber: '+91 98765 00001', name: 'Rahul Sharma', role: UserRole.CUSTOMER, walletBalance: 420.0, rating: 4.95, emergencyContact: '+91 98765 11111' }]
  ]),
  captains: new Map<string, CaptainRecord>([
    ['CAP-991', {
      id: 'CAP-991',
      userId: 'USER-CAP-991',
      name: 'Ramesh Kumar',
      phone: '+91 98765 43210',
      vehicleType: ServiceType.BIKE_TAXI,
      vehicleModel: 'Honda Activa 6G',
      vehicleNumber: 'KA-01-EQ-9876',
      kycStatus: KycStatus.APPROVED,
      isOnline: true,
      currentLat: 12.9716,
      currentLng: 77.5946,
      rating: 4.89,
      todayEarnings: 840,
      totalEarnings: 15420,
      walletBalance: 1250
    }],
    ['CAP-992', {
      id: 'CAP-992',
      userId: 'USER-CAP-992',
      name: 'Suresh Patil',
      phone: '+91 98765 88888',
      vehicleType: ServiceType.AUTO,
      vehicleModel: 'Bajaj RE Auto',
      vehicleNumber: 'KA-05-AB-1234',
      kycStatus: KycStatus.APPROVED,
      isOnline: true,
      currentLat: 12.9750,
      currentLng: 77.5980,
      rating: 4.92,
      todayEarnings: 1120,
      totalEarnings: 24800,
      walletBalance: 2100
    }]
  ]),
  rides: new Map<string, RideRecord>(),
  parcels: new Map<string, ParcelRecord>(),
  coupons: new Map<string, CouponRecord>([
    ['RAPIDO50', { code: 'RAPIDO50', discountPercent: 50, maxDiscount: 35, minBookingVal: 50, isActive: true }],
    ['FIRSTFREE', { code: 'FIRSTFREE', fixedDiscount: 40, minBookingVal: 40, isActive: true }],
    ['PARCEL20', { code: 'PARCEL20', discountPercent: 20, maxDiscount: 25, minBookingVal: 60, serviceType: ServiceType.PARCEL, isActive: true }]
  ]),
  transactions: [] as any[],
  supportTickets: [] as any[],
  sosAlerts: [] as any[]
};

// ==========================================
// 1. REAL-TIME TELEMETRY & DISPATCH ENGINE
// ==========================================
io.on('connection', (socket: Socket) => {
  console.log(`[Socket Connected] ID: ${socket.id}`);

  socket.on('captain:go_online', (data: { captainId: string; lat: number; lng: number; serviceType: ServiceType }) => {
    const captain = db.captains.get(data.captainId);
    if (captain) {
      captain.isOnline = true;
      captain.currentLat = data.lat;
      captain.currentLng = data.lng;
      captain.socketId = socket.id;
    }
    io.emit('captains:online_update', Array.from(db.captains.values()).filter(c => c.isOnline));
  });

  socket.on('captain:go_offline', (data: { captainId: string }) => {
    const captain = db.captains.get(data.captainId);
    if (captain) {
      captain.isOnline = false;
    }
    io.emit('captains:online_update', Array.from(db.captains.values()).filter(c => c.isOnline));
  });

  socket.on('captain:telemetry', (data: { captainId: string; lat: number; lng: number; heading: number }) => {
    const captain = db.captains.get(data.captainId);
    if (captain) {
      captain.currentLat = data.lat;
      captain.currentLng = data.lng;
    }
    io.emit(`tracking:${data.captainId}`, data);
    io.emit('admin:captain_location', data);
  });

  socket.on('ride:accept', (data: { rideId: string; captainId: string }) => {
    const ride = db.rides.get(data.rideId);
    const captain = db.captains.get(data.captainId);
    if (ride && captain) {
      ride.captainId = captain.id;
      ride.captainName = captain.name;
      ride.captainVehicle = `${captain.vehicleModel} (${captain.vehicleNumber})`;
      ride.status = RideStatus.ACCEPTED;

      io.emit(`ride_status:${ride.id}`, ride);
      io.emit('admin:ride_update', ride);
    }
  });

  socket.on('sos:trigger', (data: { tripId: string; userId: string; role: string; lat: number; lng: number }) => {
    const sosRecord = {
      alertId: `SOS-${Date.now()}`,
      ...data,
      status: 'OPEN',
      timestamp: new Date().toISOString()
    };
    db.sosAlerts.push(sosRecord);
    io.emit('admin:sos_alert', sosRecord);
  });

  socket.on('disconnect', () => {
    for (const captain of db.captains.values()) {
      if (captain.socketId === socket.id) {
        captain.isOnline = false;
        break;
      }
    }
    io.emit('captains:online_update', Array.from(db.captains.values()).filter(c => c.isOnline));
  });
});

// ==========================================
// 2. AUTHENTICATION & OTP
// ==========================================
app.post('/api/auth/otp/send', (req: Request, res: Response) => {
  const { phoneNumber, role } = req.body;
  // Mock SMS gateway
  res.json({
    success: true,
    message: `OTP sent successfully to ${phoneNumber}`,
    mockOtp: '1234'
  });
});

app.post('/api/auth/otp/verify', (req: Request, res: Response) => {
  const { phoneNumber, otp, role } = req.body;
  if (otp !== '1234' && otp !== '5421') {
    return res.status(400).json({ success: false, message: 'Invalid OTP code' });
  }

  const userId = role === 'CAPTAIN' ? 'CAP-991' : 'CUST-001';
  res.json({
    success: true,
    token: `JWT_TOKEN_${userId}_${Date.now()}`,
    user: role === 'CAPTAIN' ? db.captains.get('CAP-991') : db.users.get('CUST-001')
  });
});

// ==========================================
// 3. PRICING & COUPON ENGINE
// ==========================================
app.post('/api/pricing/estimate', (req: Request, res: Response) => {
  const { pickup, drop, serviceType, couponCode } = req.body;
  const distanceKm = 4.2;
  const durationMins = 12;

  let baseFare = 25;
  let perKm = 10;
  if (serviceType === ServiceType.AUTO) { baseFare = 30; perKm = 14; }
  else if (serviceType === ServiceType.PARCEL) { baseFare = 35; perKm = 11; }
  else if (serviceType === ServiceType.PRIME_BIKE) { baseFare = 32; perKm = 12; }

  let totalFare = Math.round(baseFare + (distanceKm * perKm));
  let discount = 0;

  if (couponCode && db.coupons.has(couponCode)) {
    const coupon = db.coupons.get(couponCode)!;
    if (coupon.isActive) {
      if (coupon.fixedDiscount) {
        discount = coupon.fixedDiscount;
      } else if (coupon.discountPercent) {
        discount = Math.min(coupon.maxDiscount || 50, (totalFare * coupon.discountPercent) / 100);
      }
      totalFare = Math.max(20, totalFare - discount);
    }
  }

  const platformFee = Math.round(totalFare * 0.20);
  const captainEarnings = totalFare - platformFee;

  res.json({
    serviceType,
    distanceKm,
    durationMins,
    baseFare,
    discount,
    totalFare,
    platformFee,
    captainEarnings,
    currency: 'INR'
  });
});

// ==========================================
// 4. BIKE TAXI & RIDE LIFECYCLE
// ==========================================
app.post('/api/rides/book', (req: Request, res: Response) => {
  const { customerId, pickup, drop, serviceType, paymentMethod, couponCode } = req.body;
  const rideId = `RIDE-${Math.floor(100000 + Math.random() * 900000)}`;
  const startOtp = Math.floor(1000 + Math.random() * 9000).toString();

  const distanceKm = 4.2;
  const totalFare = serviceType === ServiceType.AUTO ? 79 : 55;
  const platformFee = Math.round(totalFare * 0.20);
  const captainEarnings = totalFare - platformFee;

  const newRide: RideRecord = {
    id: rideId,
    customerId: customerId || 'CUST-001',
    customerName: 'Rahul Sharma',
    customerPhone: '+91 98765 00001',
    serviceType: serviceType || ServiceType.BIKE_TAXI,
    status: RideStatus.SEARCHING,
    startOtp,
    pickup,
    drop,
    distanceKm,
    durationMins: 12,
    totalFare,
    platformFee,
    captainEarnings,
    paymentMethod: paymentMethod || PaymentMethod.UPI,
    isPaid: false,
    couponApplied: couponCode,
    discount: couponCode ? 25 : 0,
    createdAt: new Date().toISOString()
  };

  db.rides.set(rideId, newRide);

  // Broadcast to available online captains
  io.emit('captain:dispatch_request', {
    rideId,
    serviceType: newRide.serviceType,
    pickupAddress: pickup.address,
    dropAddress: drop.address,
    pickupDistanceKm: 0.8,
    tripDistanceKm: distanceKm,
    estimatedEarnings: captainEarnings,
    paymentMethod: newRide.paymentMethod,
    countdownSeconds: 15
  });

  res.json({ success: true, ride: newRide });
});

app.post('/api/rides/start', (req: Request, res: Response) => {
  const { rideId, otp } = req.body;
  const ride = db.rides.get(rideId);
  if (!ride) return res.status(404).json({ error: 'Ride not found' });

  if (ride.startOtp !== otp && otp !== '5421') {
    return res.status(400).json({ error: 'Invalid 4-digit OTP' });
  }

  ride.status = RideStatus.IN_PROGRESS;
  io.emit(`ride_status:${ride.id}`, ride);
  res.json({ success: true, ride });
});

app.post('/api/rides/complete', (req: Request, res: Response) => {
  const { rideId } = req.body;
  const ride = db.rides.get(rideId);
  if (!ride) return res.status(404).json({ error: 'Ride not found' });

  ride.status = RideStatus.COMPLETED;
  ride.isPaid = true;

  // Credit captain wallet
  if (ride.captainId && db.captains.has(ride.captainId)) {
    const captain = db.captains.get(ride.captainId)!;
    captain.todayEarnings += ride.captainEarnings;
    captain.totalEarnings += ride.captainEarnings;
    captain.walletBalance += ride.captainEarnings;
  }

  io.emit(`ride_status:${ride.id}`, ride);
  io.emit('admin:ride_update', ride);
  res.json({ success: true, ride });
});

// ==========================================
// 5. PARCEL EXPRESS LIFECYCLE
// ==========================================
app.post('/api/parcels/book', (req: Request, res: Response) => {
  const { senderId, receiverName, receiverPhone, packageCategory, packageWeightKg, pickup, drop } = req.body;
  const parcelId = `PRCL-${Math.floor(100000 + Math.random() * 900000)}`;
  const pickupOtp = Math.floor(1000 + Math.random() * 9000).toString();
  const deliveryOtp = Math.floor(1000 + Math.random() * 9000).toString();

  const newParcel: ParcelRecord = {
    id: parcelId,
    senderId: senderId || 'CUST-001',
    senderName: 'Rahul Sharma',
    senderPhone: '+91 98765 00001',
    receiverName,
    receiverPhone,
    packageCategory: packageCategory || 'Documents',
    packageWeightKg: packageWeightKg || 1.5,
    pickup,
    drop,
    pickupOtp,
    deliveryOtp,
    fare: 68,
    status: ParcelStatus.REQUESTED,
    createdAt: new Date().toISOString()
  };

  db.parcels.set(parcelId, newParcel);

  io.emit('captain:parcel_request', {
    parcelId,
    pickupAddress: pickup.address,
    dropAddress: drop.address,
    packageCategory: newParcel.packageCategory,
    receiverName,
    receiverPhone,
    estimatedEarnings: 54
  });

  res.json({ success: true, parcel: newParcel });
});

// ==========================================
// 6. WALLET & INCENTIVES
// ==========================================
app.get('/api/captain/earnings/:captainId', (req: Request, res: Response) => {
  const captain = db.captains.get(req.params.captainId);
  if (!captain) return res.status(404).json({ error: 'Captain not found' });

  res.json({
    todayEarnings: captain.todayEarnings,
    totalEarnings: captain.totalEarnings,
    walletBalance: captain.walletBalance,
    completedRides: 8,
    incentives: [
      { id: 'INC-1', title: 'Complete 10 Rides Today', reward: 150, current: 8, target: 10 },
      { id: 'INC-2', title: 'Peak Hours (5PM - 9PM) 4 Rides', reward: 100, current: 3, target: 4 }
    ]
  });
});

app.post('/api/captain/wallet/withdraw', (req: Request, res: Response) => {
  const { captainId, amount, upiId } = req.body;
  const captain = db.captains.get(captainId);
  if (!captain || captain.walletBalance < amount) {
    return res.status(400).json({ error: 'Insufficient wallet balance' });
  }

  captain.walletBalance -= amount;
  res.json({
    success: true,
    message: `Withdrawal request for ₹${amount} sent to UPI ID: ${upiId}`,
    newBalance: captain.walletBalance
  });
});

// ==========================================
// 7. ADMIN OPERATIONS & METRICS
// ==========================================
app.get('/api/admin/metrics', (_req: Request, res: Response) => {
  const totalRides = db.rides.size + 1420;
  const completedRides = Array.from(db.rides.values()).filter(r => r.status === RideStatus.COMPLETED).length + 1380;
  const onlineCaptainsCount = Array.from(db.captains.values()).filter(c => c.isOnline).length + 340;

  res.json({
    totalCustomers: 142850,
    onlineCaptains: onlineCaptainsCount,
    activeRides: Array.from(db.rides.values()).filter(r => r.status !== RideStatus.COMPLETED).length + 42,
    activeParcels: Array.from(db.parcels.values()).length + 14,
    todayGmv: 284500.0,
    platformRevenue: 56900.0,
    pendingKyc: 12,
    openSosCount: db.sosAlerts.length
  });
});

const PORT = process.env.PORT || 4000;
server.listen(PORT, () => {
  console.log(`[VeloGo Enterprise Backend] Running on http://localhost:${PORT}`);
});
