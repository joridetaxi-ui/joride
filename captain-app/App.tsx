import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  Modal,
  Alert,
  ScrollView,
  Dimensions
} from 'react-native';
import io from 'socket.io-client';

const BACKEND_URL = 'http://localhost:4000';
const socket = io(BACKEND_URL);

type CaptainTab = 'DUTY' | 'EARNINGS' | 'INCENTIVES' | 'KYC_DOCS' | 'PROFILE';

export default function CaptainApp() {
  const [activeTab, setActiveTab] = useState<CaptainTab>('DUTY');
  const [isOnline, setIsOnline] = useState(false);
  const [kycStatus, setKycStatus] = useState<'PENDING' | 'APPROVED'>('APPROVED');
  
  // Trip & Dispatch State
  const [incomingRequest, setIncomingRequest] = useState<any>(null);
  const [activeTrip, setActiveTrip] = useState<any>(null);
  const [otpInput, setOtpInput] = useState('');
  const [deliveryProofPhoto, setDeliveryProofPhoto] = useState<boolean>(false);
  
  // Earnings & Wallet State
  const [todayEarnings, setTodayEarnings] = useState(840);
  const [walletBalance, setWalletBalance] = useState(1250);
  const [completedTrips, setCompletedTrips] = useState(6);
  const [withdrawAmount, setWithdrawAmount] = useState('');
  const [upiId, setUpiId] = useState('ramesh@okaxis');
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);

  // KYC Docs State
  const [kycDocs, setKycDocs] = useState({
    drivingLicense: true,
    rcBook: true,
    vehicleInsurance: true,
    policeVerification: true
  });

  useEffect(() => {
    socket.on('captain:dispatch_request', (data) => {
      if (isOnline && !activeTrip) {
        setIncomingRequest(data);
      }
    });

    socket.on('captain:parcel_request', (data) => {
      if (isOnline && !activeTrip) {
        setIncomingRequest({ ...data, serviceType: 'PARCEL' });
      }
    });

    return () => {
      socket.off('captain:dispatch_request');
      socket.off('captain:parcel_request');
    };
  }, [isOnline, activeTrip]);

  const toggleDuty = () => {
    const nextState = !isOnline;
    setIsOnline(nextState);
    if (nextState) {
      socket.emit('captain:go_online', {
        captainId: 'CAP-991',
        lat: 12.9716,
        lng: 77.5946,
        serviceType: 'BIKE_TAXI'
      });
      Alert.alert('Online Mode Active', 'You are now visible to nearby riders. Keep app open to receive bookings.');
    } else {
      socket.emit('captain:go_offline', { captainId: 'CAP-991' });
    }
  };

  const handleAcceptRide = () => {
    const trip = {
      ...incomingRequest,
      status: 'NAVIGATING_TO_PICKUP',
      startOtp: '5421',
      pickupOtp: '8912',
      deliveryOtp: '3491'
    };
    setActiveTrip(trip);
    socket.emit('ride:accept', { rideId: incomingRequest.rideId || incomingRequest.parcelId, captainId: 'CAP-991' });
    setIncomingRequest(null);
  };

  const handleVerifyOtp = () => {
    const requiredOtp = activeTrip?.serviceType === 'PARCEL' ? activeTrip?.pickupOtp : activeTrip?.startOtp;
    if (otpInput === requiredOtp || otpInput === '5421' || otpInput === '8912') {
      setActiveTrip({ ...activeTrip, status: 'IN_TRANSIT' });
      setOtpInput('');
      Alert.alert('OTP Verified', 'Ride started. Navigate safely to destination.');
    } else {
      Alert.alert('Invalid OTP', 'Please request rider for the 4-digit code.');
    }
  };

  const handleCompleteTrip = () => {
    const earned = activeTrip?.estimatedEarnings || 55;
    setTodayEarnings((prev) => prev + earned);
    setWalletBalance((prev) => prev + earned);
    setCompletedTrips((prev) => prev + 1);
    setActiveTrip(null);
    setOtpInput('');
    setDeliveryProofPhoto(false);
    Alert.alert('Trip Completed!', `₹${earned} has been credited to your Captain Wallet.`);
  };

  const handleWithdraw = () => {
    const amt = parseFloat(withdrawAmount);
    if (isNaN(amt) || amt <= 0 || amt > walletBalance) {
      Alert.alert('Invalid Amount', 'Please enter a valid amount within your wallet balance.');
      return;
    }
    setWalletBalance((prev) => prev - amt);
    setShowWithdrawModal(false);
    setWithdrawAmount('');
    Alert.alert('Withdrawal Processed', `₹${amt} transferred to UPI ID: ${upiId}`);
  };

  const handleDriverSOS = () => {
    socket.emit('sos:trigger', {
      tripId: activeTrip?.rideId || 'CAPTAIN_EMERGENCY',
      userId: 'CAP-991',
      role: 'CAPTAIN',
      lat: 12.9716,
      lng: 77.5946
    });
    Alert.alert('🚨 CAPTAIN SOS TRIGGERED', 'Emergency safety team notified with your location.');
  };

  return (
    <View style={styles.container}>
      {/* 1. TOP HEADER */}
      <View style={styles.header}>
        <View>
          <Text style={styles.headerTitle}>Rapido Captain</Text>
          <Text style={styles.captainSubtitle}>KA-01-EQ-9876 • ⭐️ 4.92 (Top Performer)</Text>
        </View>
        <TouchableOpacity
          style={[styles.dutyButton, isOnline ? styles.dutyOnline : styles.dutyOffline]}
          onPress={toggleDuty}
        >
          <Text style={styles.dutyText}>{isOnline ? '🟢 ON DUTY' : '🔴 OFF DUTY'}</Text>
        </TouchableOpacity>
      </View>

      {/* 2. STATS BAR */}
      <View style={styles.statsCard}>
        <View style={styles.statBox}>
          <Text style={styles.statLabel}>Today's Earnings</Text>
          <Text style={styles.statValue}>₹{todayEarnings}</Text>
        </View>
        <View style={styles.statDivider} />
        <View style={styles.statBox}>
          <Text style={styles.statLabel}>Completed</Text>
          <Text style={styles.statValue}>{completedTrips} Trips</Text>
        </View>
        <View style={styles.statDivider} />
        <View style={styles.statBox}>
          <Text style={styles.statLabel}>Wallet Balance</Text>
          <Text style={styles.statValue}>₹{walletBalance}</Text>
        </View>
      </View>

      {/* 3. MAIN BODY TABS */}
      <View style={{ flex: 1 }}>
        {/* TAB 1: DUTY & ACTIVE RADAR */}
        {activeTab === 'DUTY' && (
          <ScrollView style={styles.tabContent}>
            {!activeTrip ? (
              <View style={styles.radarCard}>
                <View style={styles.radarIconContainer}>
                  <Text style={{ fontSize: 48 }}>{isOnline ? '📡' : '💤'}</Text>
                </View>
                <Text style={styles.radarTitle}>
                  {isOnline ? 'Scanning Nearby Bookings...' : 'You are currently Offline'}
                </Text>
                <Text style={styles.radarSub}>
                  {isOnline 
                    ? 'High demand in Indiranagar & Koramangala (1.3x Surge Area)' 
                    : 'Turn duty ON to receive instant ride and parcel requests.'}
                </Text>

                {isOnline && (
                  <View style={styles.hotspotPill}>
                    <Text style={styles.hotspotText}>🔥 High Demand Hotspot: 0.6 km away</Text>
                  </View>
                )}
              </View>
            ) : (
              <View style={styles.activeTripCard}>
                <View style={styles.tripBadgeRow}>
                  <Text style={styles.tripTypeBadge}>⚡ ACTIVE {activeTrip.serviceType}</Text>
                  <TouchableOpacity style={styles.tripSosBtn} onPress={handleDriverSOS}>
                    <Text style={styles.tripSosText}>🚨 SOS</Text>
                  </TouchableOpacity>
                </View>

                <Text style={styles.earningsHighlight}>Fare: ₹{activeTrip.estimatedEarnings}</Text>

                <View style={styles.addressBox}>
                  <Text style={styles.addressLine}>🟢 Pickup: {activeTrip.pickupAddress}</Text>
                  <Text style={styles.addressLine}>🔴 Drop: {activeTrip.dropAddress}</Text>
                </View>

                {activeTrip.status === 'NAVIGATING_TO_PICKUP' ? (
                  <View style={styles.otpSection}>
                    <Text style={styles.otpLabel}>Ask Rider for Start/Pickup OTP:</Text>
                    <TextInput
                      style={styles.otpInput}
                      placeholder="4-digit OTP"
                      placeholderTextColor="#64748B"
                      keyboardType="number-pad"
                      maxLength={4}
                      value={otpInput}
                      onChangeText={setOtpInput}
                    />
                    <TouchableOpacity style={styles.primaryActionButton} onPress={handleVerifyOtp}>
                      <Text style={styles.primaryActionText}>VERIFY & START TRIP</Text>
                    </TouchableOpacity>
                  </View>
                ) : (
                  <View style={{ marginTop: 16 }}>
                    {activeTrip.serviceType === 'PARCEL' && (
                      <TouchableOpacity 
                        style={[styles.proofButton, deliveryProofPhoto && styles.proofButtonDone]}
                        onPress={() => {
                          setDeliveryProofPhoto(true);
                          Alert.alert('Photo Captured', 'Proof of delivery image verified.');
                        }}
                      >
                        <Text style={styles.proofButtonText}>
                          {deliveryProofPhoto ? '✅ Delivery Photo Attached' : '📸 Snap Proof of Delivery Photo'}
                        </Text>
                      </TouchableOpacity>
                    )}

                    <TouchableOpacity 
                      style={[styles.primaryActionButton, { backgroundColor: '#10B981', marginTop: 10 }]} 
                      onPress={handleCompleteTrip}
                    >
                      <Text style={styles.primaryActionText}>COLLECT PAYMENT & COMPLETE TRIP</Text>
                    </TouchableOpacity>
                  </View>
                )}
              </View>
            )}

            {/* Quick Safety Banner */}
            <View style={styles.captainSafetyBanner}>
              <Text style={{ fontSize: 24 }}>🛡️</Text>
              <View style={{ marginLeft: 12, flex: 1 }}>
                <Text style={styles.safetyBannerTitle}>Captain Accidental Insurance</Text>
                <Text style={styles.safetyBannerSub}>Active coverage of ₹5,00,000 during active trips.</Text>
              </View>
            </View>
          </ScrollView>
        )}

        {/* TAB 2: EARNINGS & WALLET */}
        {activeTab === 'EARNINGS' && (
          <ScrollView style={styles.tabContent}>
            <View style={styles.walletBox}>
              <Text style={styles.walletTitle}>Settlement Wallet</Text>
              <Text style={styles.walletValue}>₹{walletBalance}</Text>
              <Text style={styles.walletSub}>Linked UPI: {upiId}</Text>
              <TouchableOpacity 
                style={styles.withdrawBtn}
                onPress={() => setShowWithdrawModal(true)}
              >
                <Text style={styles.withdrawBtnText}>⚡ INSTANT UPI WITHDRAWAL</Text>
              </TouchableOpacity>
            </View>

            <Text style={styles.sectionHeader}>TODAY'S TRIP BREAKDOWN</Text>
            {[
              { id: 'TRIP-901', time: '10:15 AM', type: 'Bike Taxi', earned: '₹55' },
              { id: 'TRIP-902', time: '11:40 AM', type: 'Auto', earned: '₹79' },
              { id: 'TRIP-903', time: '01:10 PM', type: 'Parcel Express', earned: '₹68' },
              { id: 'TRIP-904', time: '02:30 PM', type: 'Bike Taxi (Peak Surge)', earned: '₹72' }
            ].map((item, i) => (
              <View key={i} style={styles.tripHistoryRow}>
                <View>
                  <Text style={styles.tripHistoryTitle}>{item.type} ({item.id})</Text>
                  <Text style={styles.tripHistoryTime}>{item.time}</Text>
                </View>
                <Text style={styles.tripHistoryEarned}>+{item.earned}</Text>
              </View>
            ))}
          </ScrollView>
        )}

        {/* TAB 3: DAILY INCENTIVES */}
        {activeTab === 'INCENTIVES' && (
          <ScrollView style={styles.tabContent}>
            <Text style={styles.sectionHeader}>DAILY & WEEKLY TARGETS</Text>
            
            <View style={styles.incentiveCard}>
              <View style={styles.incHeader}>
                <Text style={styles.incTitle}>🎯 Daily Milestone: 10 Rides</Text>
                <Text style={styles.incReward}>+₹150 Bonus</Text>
              </View>
              <Text style={styles.incSub}>Complete 10 rides today before 11:59 PM</Text>
              <View style={styles.progressBarBg}>
                <View style={[styles.progressBarFill, { width: `${(completedTrips / 10) * 100}%` }]} />
              </View>
              <Text style={styles.progressText}>{completedTrips} of 10 completed (4 more to go)</Text>
            </View>

            <View style={styles.incentiveCard}>
              <View style={styles.incHeader}>
                <Text style={styles.incTitle}>⚡ Peak Hour Rush: 5PM - 9PM</Text>
                <Text style={styles.incReward}>+₹100 Bonus</Text>
              </View>
              <Text style={styles.incSub}>Complete 4 rides during peak evening rush</Text>
              <View style={styles.progressBarBg}>
                <View style={[styles.progressBarFill, { width: '50%' }]} />
              </View>
              <Text style={styles.progressText}>2 of 4 completed</Text>
            </View>
          </ScrollView>
        )}

        {/* TAB 4: KYC & DOCUMENTS */}
        {activeTab === 'KYC_DOCS' && (
          <ScrollView style={styles.tabContent}>
            <View style={styles.kycStatusCard}>
              <Text style={styles.kycStatusTitle}>KYC Status: ✅ APPROVED</Text>
              <Text style={styles.kycStatusSub}>Your profile is verified and active for all ride categories.</Text>
            </View>

            <Text style={styles.sectionHeader}>UPLOADED DOCUMENTS</Text>
            {[
              { label: 'Driving License', status: 'Verified', date: 'Valid till 2032' },
              { label: 'Vehicle RC Certificate', status: 'Verified', date: 'KA-01-EQ-9876' },
              { label: 'Vehicle Commercial Insurance', status: 'Verified', date: 'Valid till Nov 2026' },
              { label: 'Aadhaar Card / Identity', status: 'Verified', date: 'XXXX-XXXX-8921' }
            ].map((doc, idx) => (
              <View key={idx} style={styles.docRow}>
                <View>
                  <Text style={styles.docLabel}>📄 {doc.label}</Text>
                  <Text style={styles.docSub}>{doc.date}</Text>
                </View>
                <View style={styles.verifiedBadge}>
                  <Text style={styles.verifiedText}>VERIFIED</Text>
                </View>
              </View>
            ))}
          </ScrollView>
        )}
      </View>

      {/* 4. BOTTOM NAVIGATION BAR */}
      <View style={styles.bottomNav}>
        {[
          { key: 'DUTY', label: 'Duty', icon: '🛵' },
          { key: 'EARNINGS', label: 'Earnings', icon: '💰' },
          { key: 'INCENTIVES', label: 'Incentives', icon: '🎯' },
          { key: 'KYC_DOCS', label: 'KYC', icon: '🪪' }
        ].map((tab) => (
          <TouchableOpacity
            key={tab.key}
            style={styles.navItem}
            onPress={() => setActiveTab(tab.key as CaptainTab)}
          >
            <Text style={{ fontSize: 20 }}>{tab.icon}</Text>
            <Text style={[styles.navLabel, activeTab === tab.key && styles.navLabelActive]}>
              {tab.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* 5. INCOMING DISPATCH MODAL (15-SECOND COUNTDOWN) */}
      <Modal visible={!!incomingRequest} transparent animationType="slide">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalHeaderBadge}>⚡ NEW {incomingRequest?.serviceType || 'RIDE'} REQUEST</Text>
            <Text style={styles.modalEarnings}>₹{incomingRequest?.estimatedEarnings || 55}</Text>
            <Text style={styles.modalSub}>Estimated Earnings • {incomingRequest?.tripDistanceKm || 4.2} km</Text>

            <View style={styles.routeBox}>
              <Text style={styles.routeText}>🟢 Pickup (0.8 km away): {incomingRequest?.pickupAddress}</Text>
              <Text style={styles.routeText}>🔴 Drop: {incomingRequest?.dropAddress}</Text>
            </View>

            <View style={styles.modalButtonsRow}>
              <TouchableOpacity 
                style={styles.rejectBtn}
                onPress={() => setIncomingRequest(null)}
              >
                <Text style={styles.rejectBtnText}>PASS</Text>
              </TouchableOpacity>

              <TouchableOpacity 
                style={styles.acceptBtn}
                onPress={handleAcceptRide}
              >
                <Text style={styles.acceptBtnText}>ACCEPT (15s)</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* 6. WITHDRAWAL MODAL */}
      <Modal visible={showWithdrawModal} transparent animationType="fade">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Instant UPI Withdrawal</Text>
            <Text style={styles.modalSub}>Available Balance: ₹{walletBalance}</Text>
            
            <TextInput
              style={styles.withdrawInput}
              placeholder="Amount (e.g. 500)"
              placeholderTextColor="#64748B"
              keyboardType="number-pad"
              value={withdrawAmount}
              onChangeText={setWithdrawAmount}
            />
            <TextInput
              style={styles.withdrawInput}
              placeholder="UPI ID"
              placeholderTextColor="#64748B"
              value={upiId}
              onChangeText={setUpiId}
            />

            <View style={{ flexDirection: 'row', gap: 10, marginTop: 12 }}>
              <TouchableOpacity 
                style={[styles.rejectBtn, { padding: 14 }]}
                onPress={() => setShowWithdrawModal(false)}
              >
                <Text style={styles.rejectBtnText}>CANCEL</Text>
              </TouchableOpacity>
              <TouchableOpacity 
                style={[styles.acceptBtn, { padding: 14 }]}
                onPress={handleWithdraw}
              >
                <Text style={styles.acceptBtnText}>TRANSFER NOW</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A', paddingTop: 36 },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 10, borderBottomWidth: 1, borderColor: '#1E293B' },
  headerTitle: { fontSize: 18, fontWeight: 'bold', color: '#FFFFFF' },
  captainSubtitle: { fontSize: 11, color: '#94A3B8', marginTop: 2 },
  dutyButton: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20 },
  dutyOnline: { backgroundColor: '#10B981' },
  dutyOffline: { backgroundColor: '#334155' },
  dutyText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 12 },
  statsCard: { flexDirection: 'row', backgroundColor: '#1E293B', margin: 16, padding: 14, borderRadius: 16, justifyContent: 'space-around', borderWidth: 1, borderColor: '#334155' },
  statBox: { alignItems: 'center' },
  statLabel: { fontSize: 11, color: '#94A3B8' },
  statValue: { fontSize: 15, fontWeight: 'bold', color: '#FACC15', marginTop: 2 },
  statDivider: { width: 1, backgroundColor: '#334155' },
  tabContent: { flex: 1, paddingHorizontal: 16 },
  radarCard: { height: 280, backgroundColor: '#1E293B', borderRadius: 20, justifyContent: 'center', alignItems: 'center', padding: 20, borderWidth: 1, borderColor: '#334155' },
  radarIconContainer: { width: 80, height: 80, borderRadius: 40, backgroundColor: '#0F172A', justifyContent: 'center', alignItems: 'center', marginBottom: 16 },
  radarTitle: { fontSize: 16, fontWeight: 'bold', color: '#FFFFFF' },
  radarSub: { fontSize: 12, color: '#94A3B8', textAlign: 'center', marginTop: 6, lineHeight: 16 },
  hotspotPill: { backgroundColor: '#F59E0B22', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 14, marginTop: 14, borderWidth: 1, borderColor: '#F59E0B' },
  hotspotText: { color: '#FACC15', fontSize: 11, fontWeight: 'bold' },
  activeTripCard: { backgroundColor: '#1E293B', padding: 20, borderRadius: 20, borderWidth: 1, borderColor: '#334155' },
  tripBadgeRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  tripTypeBadge: { color: '#FACC15', fontWeight: 'bold', fontSize: 14 },
  tripSosBtn: { backgroundColor: '#DC2626', paddingHorizontal: 10, paddingVertical: 4, borderRadius: 12 },
  tripSosText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 11 },
  earningsHighlight: { fontSize: 28, fontWeight: 'bold', color: '#10B981', marginVertical: 8 },
  addressBox: { backgroundColor: '#0F172A', padding: 14, borderRadius: 12, marginVertical: 10 },
  addressLine: { color: '#E2E8F0', fontSize: 13, marginVertical: 3 },
  otpSection: { marginTop: 10 },
  otpLabel: { color: '#94A3B8', fontSize: 12, marginBottom: 6 },
  otpInput: { backgroundColor: '#0F172A', color: '#FFFFFF', borderRadius: 12, padding: 12, textAlign: 'center', fontSize: 20, fontWeight: 'bold', letterSpacing: 6, borderWidth: 1, borderColor: '#334155', marginBottom: 12 },
  primaryActionButton: { backgroundColor: '#FACC15', padding: 16, borderRadius: 14, alignItems: 'center' },
  primaryActionText: { color: '#0F172A', fontWeight: 'bold', fontSize: 15 },
  proofButton: { backgroundColor: '#334155', padding: 14, borderRadius: 12, alignItems: 'center', borderWidth: 1, borderColor: '#475569' },
  proofButtonDone: { backgroundColor: '#065F46', borderColor: '#10B981' },
  proofButtonText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 13 },
  captainSafetyBanner: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#1E293B', marginTop: 16, padding: 14, borderRadius: 16, borderWidth: 1, borderColor: '#334155' },
  safetyBannerTitle: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 13 },
  safetyBannerSub: { color: '#94A3B8', fontSize: 11, marginTop: 2 },
  walletBox: { backgroundColor: '#1E293B', padding: 24, borderRadius: 20, alignItems: 'center', borderWidth: 1, borderColor: '#334155', marginBottom: 20 },
  walletTitle: { color: '#94A3B8', fontSize: 12 },
  walletValue: { color: '#10B981', fontSize: 36, fontWeight: 'bold', marginVertical: 6 },
  walletSub: { color: '#64748B', fontSize: 11, marginBottom: 14 },
  withdrawBtn: { backgroundColor: '#FACC15', paddingHorizontal: 20, paddingVertical: 12, borderRadius: 20 },
  withdrawBtnText: { color: '#0F172A', fontWeight: 'bold', fontSize: 12 },
  sectionHeader: { color: '#94A3B8', fontSize: 11, fontWeight: 'bold', letterSpacing: 1, marginBottom: 12 },
  tripHistoryRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#1E293B', padding: 14, borderRadius: 12, marginBottom: 8, borderWidth: 1, borderColor: '#334155' },
  tripHistoryTitle: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 13 },
  tripHistoryTime: { color: '#64748B', fontSize: 11, marginTop: 2 },
  tripHistoryEarned: { color: '#10B981', fontWeight: 'bold', fontSize: 14 },
  incentiveCard: { backgroundColor: '#1E293B', padding: 16, borderRadius: 16, borderWidth: 1, borderColor: '#334155', marginBottom: 12 },
  incHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  incTitle: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 14 },
  incReward: { color: '#FACC15', fontWeight: 'bold', fontSize: 13 },
  incSub: { color: '#94A3B8', fontSize: 11, marginTop: 4, marginBottom: 10 },
  progressBarBg: { height: 8, backgroundColor: '#0F172A', borderRadius: 4, overflow: 'hidden' },
  progressBarFill: { height: '100%', backgroundColor: '#10B981' },
  progressText: { color: '#64748B', fontSize: 11, marginTop: 6 },
  kycStatusCard: { backgroundColor: '#064E3B', padding: 16, borderRadius: 16, marginBottom: 20, borderWidth: 1, borderColor: '#059669' },
  kycStatusTitle: { color: '#34D399', fontWeight: 'bold', fontSize: 15 },
  kycStatusSub: { color: '#A7F3D0', fontSize: 12, marginTop: 4 },
  docRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#1E293B', padding: 14, borderRadius: 12, marginBottom: 8, borderWidth: 1, borderColor: '#334155' },
  docLabel: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 13 },
  docSub: { color: '#64748B', fontSize: 11, marginTop: 2 },
  verifiedBadge: { backgroundColor: '#065F46', paddingHorizontal: 10, paddingVertical: 4, borderRadius: 8 },
  verifiedText: { color: '#34D399', fontSize: 10, fontWeight: 'bold' },
  bottomNav: { flexDirection: 'row', backgroundColor: '#1E293B', borderTopWidth: 1, borderColor: '#334155', paddingVertical: 10 },
  navItem: { flex: 1, alignItems: 'center' },
  navLabel: { color: '#64748B', fontSize: 11, marginTop: 2, fontWeight: '600' },
  navLabelActive: { color: '#FACC15' },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.85)', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: '#1E293B', padding: 24, borderTopLeftRadius: 24, borderTopRightRadius: 24, borderWidth: 1, borderColor: '#334155' },
  modalHeaderBadge: { color: '#FACC15', fontWeight: 'bold', fontSize: 13, textAlign: 'center' },
  modalEarnings: { fontSize: 36, fontWeight: 'bold', color: '#10B981', textAlign: 'center', marginVertical: 6 },
  modalSub: { color: '#94A3B8', fontSize: 12, textAlign: 'center', marginBottom: 14 },
  routeBox: { backgroundColor: '#0F172A', padding: 14, borderRadius: 14, marginBottom: 18 },
  routeText: { color: '#E2E8F0', fontSize: 13, marginVertical: 4 },
  modalButtonsRow: { flexDirection: 'row', gap: 10 },
  rejectBtn: { flex: 1, backgroundColor: '#334155', padding: 16, borderRadius: 14, alignItems: 'center' },
  rejectBtnText: { color: '#EF4444', fontWeight: 'bold' },
  acceptBtn: { flex: 2, backgroundColor: '#10B981', padding: 16, borderRadius: 14, alignItems: 'center' },
  acceptBtnText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 16 },
  modalTitle: { fontSize: 18, fontWeight: 'bold', color: '#FFFFFF', textAlign: 'center' },
  withdrawInput: { backgroundColor: '#0F172A', color: '#FFFFFF', borderWidth: 1, borderColor: '#334155', borderRadius: 12, padding: 12, marginTop: 12, fontSize: 14 }
});
