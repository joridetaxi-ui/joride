import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  ScrollView,
  Alert,
  Modal,
  Image,
  Dimensions
} from 'react-native';
import io from 'socket.io-client';

const { width } = Dimensions.get('window');
const BACKEND_URL = 'http://localhost:4000';
const socket = io(BACKEND_URL);

type CustomerScreenType = 
  | 'HOME' 
  | 'DROP_SEARCH' 
  | 'VEHICLE_SELECT' 
  | 'LIVE_TRACKING' 
  | 'PARCEL_BOOK' 
  | 'PARCEL_TRACKING' 
  | 'PAYMENTS_WALLET' 
  | 'COUPONS' 
  | 'RIDE_HISTORY' 
  | 'SOS_SAFETY' 
  | 'PROFILE';

export default function CustomerApp() {
  const [currentScreen, setCurrentScreen] = useState<CustomerScreenType>('HOME');
  const [pickup, setPickup] = useState({ title: 'Current Location', address: '7-16-160/2, Britan St, Abm Children Park' });
  const [drop, setDrop] = useState({ title: 'Indiranagar 100ft Road', address: '12th Main, HAL 2nd Stage, Bengaluru' });
  const [selectedVehicle, setSelectedVehicle] = useState<'BIKE_TAXI' | 'AUTO' | 'PRIME_BIKE'>('BIKE_TAXI');
  const [paymentMethod, setPaymentMethod] = useState<'UPI' | 'CASH' | 'WALLET'>('UPI');
  const [appliedCoupon, setAppliedCoupon] = useState<string | null>(null);
  const [walletBalance, setWalletBalance] = useState(420);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Active Ride & Parcel State
  const [activeRide, setActiveRide] = useState<any>(null);
  const [activeParcel, setActiveParcel] = useState<any>(null);
  
  // Parcel Form State
  const [receiverName, setReceiverName] = useState('');
  const [receiverPhone, setReceiverPhone] = useState('');
  const [packageCategory, setPackageCategory] = useState('Documents');
  const [packageWeight, setPackageWeight] = useState('1.0');

  // Rating Modal
  const [showRatingModal, setShowRatingModal] = useState(false);
  const [givenRating, setGivenRating] = useState(5);

  useEffect(() => {
    socket.on(`ride_status:${activeRide?.id}`, (updatedRide) => {
      setActiveRide(updatedRide);
      if (updatedRide.status === 'COMPLETED') {
        setShowRatingModal(true);
      }
    });

    socket.on(`tracking:${activeRide?.captainId}`, (telemetry) => {
      console.log('Captain GPS updated:', telemetry);
    });

    return () => {
      socket.off(`ride_status:${activeRide?.id}`);
    };
  }, [activeRide]);

  // Calculate Real-Time Fare
  const calculateFare = (type: string) => {
    let base = type === 'AUTO' ? 79 : type === 'PRIME_BIKE' ? 68 : 55;
    if (appliedCoupon === 'RAPIDO50') base = Math.round(base * 0.5);
    if (appliedCoupon === 'FIRSTFREE') base = Math.max(15, base - 40);
    return base;
  };

  const handleBookRide = () => {
    const newRide = {
      id: `RIDE-${Math.floor(100000 + Math.random() * 900000)}`,
      pickup,
      drop,
      serviceType: selectedVehicle,
      totalFare: calculateFare(selectedVehicle),
      paymentMethod,
      startOtp: '5421',
      status: 'SEARCHING',
      captainName: 'Ramesh Kumar',
      captainPhone: '+91 98765 43210',
      captainVehicle: 'Honda Activa (KA-01-EQ-9876)',
      rating: 4.89
    };
    setActiveRide(newRide);
    setCurrentScreen('LIVE_TRACKING');

    socket.emit('ride:request', newRide);
  };

  const handleBookParcel = () => {
    if (!receiverName || !receiverPhone) {
      Alert.alert('Missing Details', 'Please enter recipient name and phone number.');
      return;
    }
    const newParcel = {
      id: `PRCL-${Math.floor(100000 + Math.random() * 900000)}`,
      pickup,
      drop,
      receiverName,
      receiverPhone,
      packageCategory,
      packageWeightKg: parseFloat(packageWeight) || 1.0,
      pickupOtp: '8912',
      deliveryOtp: '3491',
      fare: 68,
      status: 'SEARCHING_CAPTAIN'
    };
    setActiveParcel(newParcel);
    setCurrentScreen('PARCEL_TRACKING');
  };

  const handleTriggerSOS = () => {
    socket.emit('sos:trigger', {
      tripId: activeRide?.id || activeParcel?.id || 'DIRECT_SOS',
      userId: 'CUST-001',
      role: 'CUSTOMER',
      lat: 12.9716,
      lng: 77.5946
    });
    Alert.alert('🚨 SOS DISPATCHED', 'Emergency safety team & nearest police patrol notified with your live GPS location.');
  };

  return (
    <View style={styles.container}>
      {/* TOP APP HEADER */}
      <View style={styles.topHeader}>
        <View style={styles.headerLeft}>
          <TouchableOpacity onPress={() => setCurrentScreen('PROFILE')}>
            <View style={styles.avatarCircle}>
              <Text style={styles.avatarText}>RS</Text>
            </View>
          </TouchableOpacity>
          <View style={{ marginLeft: 10 }}>
            <Text style={styles.appName}>Rapido</Text>
            <Text style={styles.appSub}>Customer Rider</Text>
          </View>
        </View>

        <View style={styles.headerRight}>
          <TouchableOpacity 
            style={styles.sosButton} 
            onPress={() => setCurrentScreen('SOS_SAFETY')}
          >
            <Text style={styles.sosButtonText}>🛡️ SOS</Text>
          </TouchableOpacity>

          <TouchableOpacity 
            style={styles.walletPill}
            onPress={() => setCurrentScreen('PAYMENTS_WALLET')}
          >
            <Text style={styles.walletPillText}>₹{walletBalance}</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* BODY CONTENT BASED ON SCREEN */}
      <View style={{ flex: 1 }}>
        {/* 1. HOME SCREEN */}
        {currentScreen === 'HOME' && (
          <ScrollView style={styles.scrollContent}>
            {/* Live Map Canvas Simulation */}
            <View style={styles.mapContainer}>
              <View style={styles.mapPinContainer}>
                <View style={styles.pickupBadge}>
                  <Text style={styles.pickupBadgeText}>Pickup Point</Text>
                </View>
                <View style={styles.pinNeedle} />
                <View style={styles.pinDot} />
              </View>
              
              <View style={styles.currentAddressPill}>
                <View style={styles.greenDot} />
                <Text numberOfLines={1} style={styles.currentAddressText}>
                  {pickup.address}
                </Text>
              </View>
            </View>

            {/* "Where do you want to go?" Card */}
            <TouchableOpacity 
              style={styles.searchCard}
              onPress={() => setCurrentScreen('DROP_SEARCH')}
            >
              <Text style={styles.searchIcon}>🔍</Text>
              <Text style={styles.searchPlaceholder}>Where do you want to go?</Text>
            </TouchableOpacity>

            {/* Service Grid */}
            <View style={styles.serviceGrid}>
              <TouchableOpacity 
                style={styles.serviceCard} 
                onPress={() => { setSelectedVehicle('BIKE_TAXI'); setCurrentScreen('DROP_SEARCH'); }}
              >
                <Text style={styles.serviceEmoji}>🛵</Text>
                <Text style={styles.serviceName}>Bike Taxi</Text>
                <Text style={styles.serviceBadge}>Fastest</Text>
              </TouchableOpacity>

              <TouchableOpacity 
                style={styles.serviceCard} 
                onPress={() => { setSelectedVehicle('AUTO'); setCurrentScreen('DROP_SEARCH'); }}
              >
                <Text style={styles.serviceEmoji}>🛺</Text>
                <Text style={styles.serviceName}>Auto</Text>
                <Text style={styles.serviceBadge}>Pocket friendly</Text>
              </TouchableOpacity>

              <TouchableOpacity 
                style={styles.serviceCard} 
                onPress={() => setCurrentScreen('PARCEL_BOOK')}
              >
                <Text style={styles.serviceEmoji}>📦</Text>
                <Text style={styles.serviceName}>Parcel</Text>
                <Text style={styles.serviceBadge}>Express</Text>
              </TouchableOpacity>
            </View>

            {/* Refer a friend banner */}
            <View style={styles.referCard}>
              <View style={{ flex: 1 }}>
                <Text style={styles.referTitle}>Enjoying Rapido rides?</Text>
                <Text style={styles.referSub}>Invite friends and get ₹50 free ride credits!</Text>
                <Text style={styles.referLink}>Refer a Friend  ›</Text>
              </View>
              <Text style={{ fontSize: 36 }}>🎁</Text>
            </View>
          </ScrollView>
        )}

        {/* 2. DROP SEARCH SCREEN */}
        {currentScreen === 'DROP_SEARCH' && (
          <View style={styles.screenInner}>
            <View style={styles.searchNavHeader}>
              <TouchableOpacity onPress={() => setCurrentScreen('HOME')}>
                <Text style={styles.backBtnText}>← Back</Text>
              </TouchableOpacity>
              <Text style={styles.screenTitle}>Select Drop Location</Text>
            </View>

            <View style={styles.inputLocationGroup}>
              <View style={styles.locRow}>
                <View style={styles.greenCircle} />
                <Text style={styles.locTextFixed}>{pickup.address}</Text>
              </View>
              <View style={styles.dottedConnector} />
              <View style={styles.locRow}>
                <View style={styles.redCircle} />
                <TextInput
                  style={styles.dropInput}
                  placeholder="Type destination, mall, or landmark..."
                  value={searchQuery}
                  onChangeText={setSearchQuery}
                  autoFocus
                />
              </View>
            </View>

            <Text style={styles.suggestHeader}>POPULAR DESTINATIONS</Text>
            <ScrollView>
              {[
                { title: 'Indiranagar 100ft Road', address: '12th Main, HAL 2nd Stage, Bengaluru' },
                { title: 'Koramangala 5th Block', address: 'Near Forum Mall, Hosur Road' },
                { title: 'MG Road Metro Station', address: 'Trinity Circle, MG Road' },
                { title: 'Electronic City Phase 1', address: 'Velankani Drive, Bengaluru' }
              ].map((item, idx) => (
                <TouchableOpacity
                  key={idx}
                  style={styles.suggestionRow}
                  onPress={() => {
                    setDrop(item);
                    setCurrentScreen('VEHICLE_SELECT');
                  }}
                >
                  <Text style={{ fontSize: 20 }}>📍</Text>
                  <View style={{ marginLeft: 12, flex: 1 }}>
                    <Text style={styles.suggestTitle}>{item.title}</Text>
                    <Text style={styles.suggestSub}>{item.address}</Text>
                  </View>
                  <Text style={{ color: '#94A3B8' }}>↗</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>
        )}

        {/* 3. VEHICLE SELECTION & FARE BREAKDOWN */}
        {currentScreen === 'VEHICLE_SELECT' && (
          <View style={styles.screenInner}>
            <View style={styles.searchNavHeader}>
              <TouchableOpacity onPress={() => setCurrentScreen('DROP_SEARCH')}>
                <Text style={styles.backBtnText}>← Change Drop</Text>
              </TouchableOpacity>
              <Text style={styles.screenTitle}>Choose Ride</Text>
            </View>

            <View style={styles.routeSummaryCard}>
              <Text style={styles.routeSummaryPickup}>🟢 {pickup.address}</Text>
              <Text style={styles.routeSummaryDrop}>🔴 {drop.title} ({drop.address})</Text>
            </View>

            {/* Vehicle Options */}
            <TouchableOpacity
              style={[styles.vehicleOptionCard, selectedVehicle === 'BIKE_TAXI' && styles.vehicleOptionSelected]}
              onPress={() => setSelectedVehicle('BIKE_TAXI')}
            >
              <Text style={{ fontSize: 32 }}>🛵</Text>
              <View style={{ flex: 1, marginLeft: 12 }}>
                <Text style={styles.vehicleOptionTitle}>Bike Taxi</Text>
                <Text style={styles.vehicleOptionSub}>Beat traffic • 3 mins away</Text>
              </View>
              <Text style={styles.vehicleOptionPrice}>₹{calculateFare('BIKE_TAXI')}</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.vehicleOptionCard, selectedVehicle === 'AUTO' && styles.vehicleOptionSelected]}
              onPress={() => setSelectedVehicle('AUTO')}
            >
              <Text style={{ fontSize: 32 }}>🛺</Text>
              <View style={{ flex: 1, marginLeft: 12 }}>
                <Text style={styles.vehicleOptionTitle}>Auto</Text>
                <Text style={styles.vehicleOptionSub}>Spacious & safe • 5 mins away</Text>
              </View>
              <Text style={styles.vehicleOptionPrice}>₹{calculateFare('AUTO')}</Text>
            </TouchableOpacity>

            {/* Coupon & Payment Method */}
            <View style={styles.paymentSection}>
              <TouchableOpacity 
                style={styles.couponPill}
                onPress={() => setCurrentScreen('COUPONS')}
              >
                <Text style={styles.couponText}>
                  🎟️ {appliedCoupon ? `Coupon Applied: ${appliedCoupon}` : 'Apply Coupon'}
                </Text>
              </TouchableOpacity>

              <View style={styles.paymentMethodsRow}>
                {['UPI', 'CASH', 'WALLET'].map((m) => (
                  <TouchableOpacity
                    key={m}
                    style={[styles.paymentBtn, paymentMethod === m && styles.paymentBtnActive]}
                    onPress={() => setPaymentMethod(m as any)}
                  >
                    <Text style={[styles.paymentBtnText, paymentMethod === m && styles.paymentBtnTextActive]}>
                      {m}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>

            <TouchableOpacity style={styles.bookMainButton} onPress={handleBookRide}>
              <Text style={styles.bookMainButtonText}>
                BOOK {selectedVehicle.replace('_', ' ')} (₹{calculateFare(selectedVehicle)})
              </Text>
            </TouchableOpacity>
          </View>
        )}

        {/* 4. LIVE RIDE TRACKING */}
        {currentScreen === 'LIVE_TRACKING' && (
          <View style={styles.screenInner}>
            <View style={styles.liveHeader}>
              <Text style={styles.liveTitle}>Trip Status: {activeRide?.status || 'SEARCHING'}</Text>
            </View>

            <View style={styles.otpBannerCard}>
              <Text style={styles.otpBannerLabel}>GIVE THIS OTP TO CAPTAIN TO START</Text>
              <Text style={styles.otpBannerCode}>{activeRide?.startOtp || '5421'}</Text>
            </View>

            <View style={styles.driverInfoCard}>
              <Text style={{ fontSize: 40 }}>👤</Text>
              <View style={{ flex: 1, marginLeft: 14 }}>
                <Text style={styles.driverName}>{activeRide?.captainName}</Text>
                <Text style={styles.driverVehicle}>{activeRide?.captainVehicle}</Text>
                <Text style={styles.driverRating}>⭐️ {activeRide?.rating} (1,420 rides)</Text>
              </View>
            </View>

            <View style={styles.actionButtonsRow}>
              <TouchableOpacity 
                style={styles.actionCallBtn}
                onPress={() => Alert.alert('Calling Captain', 'Connecting masked call to Captain...')}
              >
                <Text style={styles.actionCallText}>📞 Call Captain</Text>
              </TouchableOpacity>

              <TouchableOpacity 
                style={styles.actionSosBtn}
                onPress={handleTriggerSOS}
              >
                <Text style={styles.actionSosText}>🚨 Emergency SOS</Text>
              </TouchableOpacity>
            </View>

            <TouchableOpacity 
              style={styles.cancelTripBtn}
              onPress={() => {
                Alert.alert('Cancel Ride', 'Are you sure you want to cancel?', [
                  { text: 'No' },
                  { text: 'Yes, Cancel', onPress: () => { setActiveRide(null); setCurrentScreen('HOME'); } }
                ]);
              }}
            >
              <Text style={styles.cancelTripText}>Cancel Ride</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* 5. PARCEL BOOKING SCREEN */}
        {currentScreen === 'PARCEL_BOOK' && (
          <ScrollView style={styles.screenInner}>
            <View style={styles.searchNavHeader}>
              <TouchableOpacity onPress={() => setCurrentScreen('HOME')}>
                <Text style={styles.backBtnText}>← Home</Text>
              </TouchableOpacity>
              <Text style={styles.screenTitle}>Book Parcel Express</Text>
            </View>

            <Text style={styles.formSectionTitle}>Recipient Details</Text>
            <TextInput
              style={styles.formInput}
              placeholder="Receiver Full Name"
              value={receiverName}
              onChangeText={setReceiverName}
            />
            <TextInput
              style={styles.formInput}
              placeholder="Receiver Mobile Number"
              keyboardType="phone-pad"
              value={receiverPhone}
              onChangeText={setReceiverPhone}
            />

            <Text style={styles.formSectionTitle}>Package Category</Text>
            <View style={styles.packageCategoryRow}>
              {['Documents', 'Food/Tiffin', 'Keys', 'Electronics'].map((cat) => (
                <TouchableOpacity
                  key={cat}
                  style={[styles.catPill, packageCategory === cat && styles.catPillActive]}
                  onPress={() => setPackageCategory(cat)}
                >
                  <Text style={[styles.catPillText, packageCategory === cat && styles.catPillTextActive]}>
                    {cat}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <View style={styles.parcelFareCard}>
              <Text style={styles.parcelFareLabel}>Estimated Delivery Fee</Text>
              <Text style={styles.parcelFareValue}>₹68</Text>
              <Text style={styles.parcelFareSub}>Includes package insurance & live OTP safety</Text>
            </View>

            <TouchableOpacity style={styles.bookMainButton} onPress={handleBookParcel}>
              <Text style={styles.bookMainButtonText}>CONFIRM & DISPATCH PARCEL</Text>
            </TouchableOpacity>
          </ScrollView>
        )}

        {/* 6. PARCEL TRACKING SCREEN */}
        {currentScreen === 'PARCEL_TRACKING' && (
          <View style={styles.screenInner}>
            <View style={styles.liveHeader}>
              <Text style={styles.liveTitle}>📦 Parcel Delivery Status</Text>
            </View>

            <View style={styles.otpGrid}>
              <View style={styles.otpGridBox}>
                <Text style={styles.otpGridLabel}>PICKUP OTP</Text>
                <Text style={styles.otpGridCode}>{activeParcel?.pickupOtp || '8912'}</Text>
              </View>
              <View style={styles.otpGridBox}>
                <Text style={styles.otpGridLabel}>DELIVERY OTP</Text>
                <Text style={styles.otpGridCode}>{activeParcel?.deliveryOtp || '3491'}</Text>
              </View>
            </View>

            <View style={styles.driverInfoCard}>
              <Text style={{ fontSize: 32 }}>🛵</Text>
              <View style={{ flex: 1, marginLeft: 14 }}>
                <Text style={styles.driverName}>Captain Assigned</Text>
                <Text style={styles.driverVehicle}>Receiver: {activeParcel?.receiverName} ({activeParcel?.receiverPhone})</Text>
                <Text style={styles.driverRating}>Category: {activeParcel?.packageCategory}</Text>
              </View>
            </View>

            <TouchableOpacity 
              style={styles.bookMainButton}
              onPress={() => {
                Alert.alert('Delivery Complete', 'Package delivered successfully!');
                setActiveParcel(null);
                setCurrentScreen('HOME');
              }}
            >
              <Text style={styles.bookMainButtonText}>Done / Back to Home</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* 7. COUPONS SCREEN */}
        {currentScreen === 'COUPONS' && (
          <View style={styles.screenInner}>
            <View style={styles.searchNavHeader}>
              <TouchableOpacity onPress={() => setCurrentScreen('VEHICLE_SELECT')}>
                <Text style={styles.backBtnText}>← Back</Text>
              </TouchableOpacity>
              <Text style={styles.screenTitle}>Available Coupons</Text>
            </View>

            {[
              { code: 'RAPIDO50', title: '50% Flat Off', desc: 'Save up to ₹35 on any Bike Taxi ride' },
              { code: 'FIRSTFREE', title: '₹40 Instant Cashback', desc: 'Flat ₹40 off on your next trip' },
              { code: 'PARCEL20', title: '20% Off on Parcel', desc: 'Valid on Express Parcel deliveries' }
            ].map((c) => (
              <View key={c.code} style={styles.couponCard}>
                <View style={{ flex: 1 }}>
                  <Text style={styles.couponCodeBadge}>{c.code}</Text>
                  <Text style={styles.couponCardTitle}>{c.title}</Text>
                  <Text style={styles.couponCardDesc}>{c.desc}</Text>
                </View>
                <TouchableOpacity 
                  style={styles.applyBtn}
                  onPress={() => {
                    setAppliedCoupon(c.code);
                    Alert.alert('Coupon Applied', `Code ${c.code} applied successfully!`);
                    setCurrentScreen('VEHICLE_SELECT');
                  }}
                >
                  <Text style={styles.applyBtnText}>APPLY</Text>
                </TouchableOpacity>
              </View>
            ))}
          </View>
        )}

        {/* 8. WALLET & PAYMENTS SCREEN */}
        {currentScreen === 'PAYMENTS_WALLET' && (
          <View style={styles.screenInner}>
            <View style={styles.searchNavHeader}>
              <TouchableOpacity onPress={() => setCurrentScreen('HOME')}>
                <Text style={styles.backBtnText}>← Home</Text>
              </TouchableOpacity>
              <Text style={styles.screenTitle}>Rapido Wallet</Text>
            </View>

            <View style={styles.walletBalanceCard}>
              <Text style={styles.walletBalanceLabel}>Available Balance</Text>
              <Text style={styles.walletBalanceAmount}>₹{walletBalance}</Text>
              <TouchableOpacity 
                style={styles.addMoneyBtn}
                onPress={() => {
                  setWalletBalance((prev) => prev + 500);
                  Alert.alert('Added ₹500', '₹500 added to your Rapido Wallet via UPI.');
                }}
              >
                <Text style={styles.addMoneyBtnText}>+ Add ₹500 via UPI</Text>
              </TouchableOpacity>
            </View>

            <Text style={styles.suggestHeader}>TRANSACTION HISTORY</Text>
            {[
              { desc: 'Bike Taxi to Indiranagar', amount: '- ₹55', date: 'Today, 2:30 PM' },
              { desc: 'Wallet Top-up (UPI)', amount: '+ ₹500', date: 'Yesterday' },
              { desc: 'Auto Ride to Koramangala', amount: '- ₹79', date: '26 Aug 2026' }
            ].map((tx, i) => (
              <View key={i} style={styles.txRow}>
                <View>
                  <Text style={styles.txDesc}>{tx.desc}</Text>
                  <Text style={styles.txDate}>{tx.date}</Text>
                </View>
                <Text style={[styles.txAmount, tx.amount.startsWith('+') ? { color: '#16A34A' } : { color: '#0F172A' }]}>
                  {tx.amount}
                </Text>
              </View>
            ))}
          </View>
        )}

        {/* 9. SOS SAFETY SCREEN */}
        {currentScreen === 'SOS_SAFETY' && (
          <View style={styles.screenInner}>
            <View style={styles.searchNavHeader}>
              <TouchableOpacity onPress={() => setCurrentScreen('HOME')}>
                <Text style={styles.backBtnText}>← Close</Text>
              </TouchableOpacity>
              <Text style={styles.screenTitle}>Safety & Emergency SOS</Text>
            </View>

            <View style={styles.sosAlertBox}>
              <Text style={styles.sosAlertTitle}>🚨 Emergency SOS Dispatch</Text>
              <Text style={styles.sosAlertDesc}>
                Pressing the button below instantly transmits your live GPS coordinates, captain details, and vehicle license number to the 24x7 Rapid Safety Response Team and Local Police.
              </Text>
              <TouchableOpacity style={styles.sosBigTrigger} onPress={handleTriggerSOS}>
                <Text style={styles.sosBigTriggerText}>TRIGGER EMERGENCY SOS</Text>
              </TouchableOpacity>
            </View>

            <Text style={styles.suggestHeader}>SAFETY FEATURES</Text>
            <View style={styles.safetyFeatureRow}>
              <Text style={{ fontSize: 24 }}>📍</Text>
              <View style={{ marginLeft: 12 }}>
                <Text style={styles.suggestTitle}>Share Live Trip with Family</Text>
                <Text style={styles.suggestSub}>Send tracking URL via WhatsApp/SMS</Text>
              </View>
            </View>
            <View style={styles.safetyFeatureRow}>
              <Text style={{ fontSize: 24 }}>📞</Text>
              <View style={{ marginLeft: 12 }}>
                <Text style={styles.suggestTitle}>Number Masking Enabled</Text>
                <Text style={styles.suggestSub}>Your real mobile number is never shown to Captains</Text>
              </View>
            </View>
          </View>
        )}
      </View>

      {/* RATING DIALOG */}
      <Modal visible={showRatingModal} transparent animationType="slide">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>How was your ride?</Text>
            <Text style={styles.modalSub}>Rate Captain {activeRide?.captainName}</Text>
            <View style={styles.starsRow}>
              {[1, 2, 3, 4, 5].map((star) => (
                <TouchableOpacity key={star} onPress={() => setGivenRating(star)}>
                  <Text style={{ fontSize: 36, marginHorizontal: 4 }}>
                    {star <= givenRating ? '⭐️' : '☆'}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
            <TouchableOpacity
              style={styles.bookMainButton}
              onPress={() => {
                setShowRatingModal(false);
                setActiveRide(null);
                setCurrentScreen('HOME');
                Alert.alert('Thank You', 'Your feedback helps improve Rapido services!');
              }}
            >
              <Text style={styles.bookMainButtonText}>SUBMIT RATING</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F8FAFC', paddingTop: 36 },
  topHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 10, backgroundColor: '#FFFFFF', borderBottomWidth: 1, borderColor: '#E2E8F0' },
  headerLeft: { flexDirection: 'row', alignItems: 'center' },
  avatarCircle: { width: 38, height: 38, borderRadius: 19, backgroundColor: '#0F172A', justifyContent: 'center', alignItems: 'center' },
  avatarText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 14 },
  appName: { fontSize: 16, fontWeight: 'bold', color: '#0F172A' },
  appSub: { fontSize: 11, color: '#64748B' },
  headerRight: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  sosButton: { backgroundColor: '#FEE2E2', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 16 },
  sosButtonText: { color: '#DC2626', fontWeight: 'bold', fontSize: 12 },
  walletPill: { backgroundColor: '#FEF3C7', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 16 },
  walletPillText: { color: '#B45309', fontWeight: 'bold', fontSize: 13 },
  scrollContent: { flex: 1, padding: 16 },
  screenInner: { flex: 1, padding: 16 },
  mapContainer: { height: 220, backgroundColor: '#E2E8F0', borderRadius: 20, justifyContent: 'center', alignItems: 'center', position: 'relative', overflow: 'hidden' },
  mapPinContainer: { alignItems: 'center' },
  pickupBadge: { backgroundColor: '#108A4E', paddingHorizontal: 14, paddingVertical: 6, borderRadius: 16 },
  pickupBadgeText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 12 },
  pinNeedle: { width: 2, height: 10, backgroundColor: '#108A4E' },
  pinDot: { width: 14, height: 14, borderRadius: 7, backgroundColor: '#108A4E', borderWidth: 2, borderColor: '#FFFFFF' },
  currentAddressPill: { position: 'absolute', bottom: 12, left: 16, right: 16, backgroundColor: '#FFFFFF', padding: 10, borderRadius: 20, flexDirection: 'row', alignItems: 'center', shadowOpacity: 0.1, shadowRadius: 4 },
  greenDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: '#108A4E', marginRight: 8 },
  currentAddressText: { fontSize: 12, color: '#1E293B', flex: 1 },
  searchCard: { backgroundColor: '#FFFFFF', marginTop: 14, padding: 16, borderRadius: 16, flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: '#E2E8F0', shadowOpacity: 0.05, shadowRadius: 6 },
  searchIcon: { fontSize: 20, marginRight: 10 },
  searchPlaceholder: { fontSize: 16, fontWeight: 'bold', color: '#0F172A' },
  serviceGrid: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 14 },
  serviceCard: { width: '31%', backgroundColor: '#FFFFFF', padding: 14, borderRadius: 16, alignItems: 'center', borderWidth: 1, borderColor: '#E2E8F0' },
  serviceEmoji: { fontSize: 28, marginBottom: 4 },
  serviceName: { fontSize: 13, fontWeight: 'bold', color: '#0F172A' },
  serviceBadge: { fontSize: 10, color: '#16A34A', marginTop: 2, fontWeight: '600' },
  referCard: { backgroundColor: '#EFF6FF', marginTop: 16, padding: 16, borderRadius: 16, flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: '#BFDBFE' },
  referTitle: { fontSize: 14, fontWeight: 'bold', color: '#1E3A8A' },
  referSub: { fontSize: 12, color: '#3B82F6', marginTop: 2 },
  referLink: { fontSize: 12, fontWeight: 'bold', color: '#2563EB', marginTop: 6 },
  searchNavHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 },
  backBtnText: { fontSize: 14, fontWeight: 'bold', color: '#2563EB' },
  screenTitle: { fontSize: 17, fontWeight: 'bold', color: '#0F172A' },
  inputLocationGroup: { backgroundColor: '#FFFFFF', padding: 16, borderRadius: 16, borderWidth: 1, borderColor: '#E2E8F0', marginBottom: 16 },
  locRow: { flexDirection: 'row', alignItems: 'center' },
  greenCircle: { width: 12, height: 12, borderRadius: 6, borderWidth: 3, borderColor: '#108A4E', marginRight: 10 },
  redCircle: { width: 12, height: 12, borderRadius: 6, borderWidth: 3, borderColor: '#EF4444', marginRight: 10 },
  locTextFixed: { fontSize: 13, color: '#475569', flex: 1 },
  dottedConnector: { width: 2, height: 14, backgroundColor: '#CBD5E1', marginLeft: 5, marginVertical: 4 },
  dropInput: { fontSize: 14, fontWeight: 'bold', color: '#0F172A', flex: 1, padding: 0 },
  suggestHeader: { fontSize: 11, fontWeight: 'bold', color: '#94A3B8', marginBottom: 10, letterSpacing: 1 },
  suggestionRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#FFFFFF', padding: 14, borderRadius: 12, marginBottom: 8, borderWidth: 1, borderColor: '#E2E8F0' },
  suggestTitle: { fontSize: 14, fontWeight: 'bold', color: '#0F172A' },
  suggestSub: { fontSize: 12, color: '#64748B', marginTop: 2 },
  routeSummaryCard: { backgroundColor: '#F1F5F9', padding: 12, borderRadius: 12, marginBottom: 14 },
  routeSummaryPickup: { fontSize: 12, color: '#334155', marginBottom: 4 },
  routeSummaryDrop: { fontSize: 12, color: '#0F172A', fontWeight: 'bold' },
  vehicleOptionCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#FFFFFF', padding: 16, borderRadius: 16, borderWidth: 1.5, borderColor: '#E2E8F0', marginBottom: 10 },
  vehicleOptionSelected: { borderColor: '#F59E0B', backgroundColor: '#FFFBEB' },
  vehicleOptionTitle: { fontSize: 16, fontWeight: 'bold', color: '#0F172A' },
  vehicleOptionSub: { fontSize: 12, color: '#64748B' },
  vehicleOptionPrice: { fontSize: 18, fontWeight: 'bold', color: '#0F172A' },
  paymentSection: { marginTop: 10 },
  couponPill: { backgroundColor: '#FEF3C7', padding: 12, borderRadius: 12, alignItems: 'center', marginBottom: 10 },
  couponText: { color: '#B45309', fontWeight: 'bold', fontSize: 13 },
  paymentMethodsRow: { flexDirection: 'row', justifyContent: 'space-between' },
  paymentBtn: { flex: 1, padding: 10, borderRadius: 10, borderWidth: 1, borderColor: '#CBD5E1', alignItems: 'center', marginHorizontal: 4 },
  paymentBtnActive: { borderColor: '#0F172A', backgroundColor: '#0F172A' },
  paymentBtnText: { fontSize: 12, fontWeight: 'bold', color: '#475569' },
  paymentBtnTextActive: { color: '#FFFFFF' },
  bookMainButton: { backgroundColor: '#FFCC00', padding: 16, borderRadius: 14, alignItems: 'center', marginTop: 16 },
  bookMainButtonText: { fontSize: 16, fontWeight: 'bold', color: '#0F172A' },
  liveHeader: { backgroundColor: '#108A4E', padding: 14, borderRadius: 12, alignItems: 'center', marginBottom: 14 },
  liveTitle: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 16 },
  otpBannerCard: { backgroundColor: '#FFFFFF', padding: 18, borderRadius: 16, alignItems: 'center', borderWidth: 1, borderColor: '#E2E8F0', marginBottom: 14 },
  otpBannerLabel: { fontSize: 11, fontWeight: 'bold', color: '#64748B', letterSpacing: 1 },
  otpBannerCode: { fontSize: 36, fontWeight: 'bold', color: '#16A34A', letterSpacing: 6, marginTop: 4 },
  driverInfoCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#FFFFFF', padding: 16, borderRadius: 16, borderWidth: 1, borderColor: '#E2E8F0', marginBottom: 16 },
  driverName: { fontSize: 16, fontWeight: 'bold', color: '#0F172A' },
  driverVehicle: { fontSize: 13, color: '#475569', marginTop: 2 },
  driverRating: { fontSize: 12, color: '#B45309', marginTop: 4, fontWeight: 'bold' },
  actionButtonsRow: { flexDirection: 'row', gap: 10, marginBottom: 12 },
  actionCallBtn: { flex: 1, backgroundColor: '#0F172A', padding: 14, borderRadius: 12, alignItems: 'center' },
  actionCallText: { color: '#FFFFFF', fontWeight: 'bold' },
  actionSosBtn: { flex: 1, backgroundColor: '#DC2626', padding: 14, borderRadius: 12, alignItems: 'center' },
  actionSosText: { color: '#FFFFFF', fontWeight: 'bold' },
  cancelTripBtn: { padding: 14, alignItems: 'center' },
  cancelTripText: { color: '#EF4444', fontWeight: 'bold' },
  formSectionTitle: { fontSize: 14, fontWeight: 'bold', color: '#0F172A', marginTop: 12, marginBottom: 8 },
  formInput: { backgroundColor: '#FFFFFF', borderWidth: 1, borderColor: '#CBD5E1', borderRadius: 12, padding: 12, marginBottom: 10, fontSize: 14 },
  packageCategoryRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  catPill: { backgroundColor: '#FFFFFF', borderWidth: 1, borderColor: '#CBD5E1', paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20 },
  catPillActive: { backgroundColor: '#0F172A', borderColor: '#0F172A' },
  catPillText: { fontSize: 12, fontWeight: '600', color: '#475569' },
  catPillTextActive: { color: '#FFFFFF' },
  parcelFareCard: { backgroundColor: '#EFF6FF', padding: 16, borderRadius: 16, alignItems: 'center', borderWidth: 1, borderColor: '#BFDBFE' },
  parcelFareLabel: { fontSize: 12, color: '#3B82F6' },
  parcelFareValue: { fontSize: 28, fontWeight: 'bold', color: '#1E3A8A', marginVertical: 4 },
  parcelFareSub: { fontSize: 11, color: '#60A5FA' },
  otpGrid: { flexDirection: 'row', gap: 10, marginBottom: 16 },
  otpGridBox: { flex: 1, backgroundColor: '#FFFFFF', padding: 16, borderRadius: 14, alignItems: 'center', borderWidth: 1, borderColor: '#E2E8F0' },
  otpGridLabel: { fontSize: 11, color: '#64748B', fontWeight: 'bold' },
  otpGridCode: { fontSize: 24, fontWeight: 'bold', color: '#2563EB', marginTop: 4 },
  couponCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#FFFFFF', padding: 16, borderRadius: 14, borderWidth: 1, borderColor: '#E2E8F0', marginBottom: 10 },
  couponCodeBadge: { backgroundColor: '#FEF3C7', color: '#B45309', fontWeight: 'bold', fontSize: 12, paddingHorizontal: 8, paddingVertical: 2, borderRadius: 6, alignSelf: 'flex-start' },
  couponCardTitle: { fontSize: 15, fontWeight: 'bold', color: '#0F172A', marginTop: 6 },
  couponCardDesc: { fontSize: 12, color: '#64748B' },
  applyBtn: { backgroundColor: '#0F172A', paddingHorizontal: 16, paddingVertical: 8, borderRadius: 10 },
  applyBtnText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 12 },
  walletBalanceCard: { backgroundColor: '#0F172A', padding: 24, borderRadius: 20, alignItems: 'center', marginBottom: 20 },
  walletBalanceLabel: { color: '#94A3B8', fontSize: 12 },
  walletBalanceAmount: { color: '#FACC15', fontSize: 38, fontWeight: 'bold', marginVertical: 6 },
  addMoneyBtn: { backgroundColor: '#FACC15', paddingHorizontal: 20, paddingVertical: 10, borderRadius: 20, marginTop: 8 },
  addMoneyBtnText: { color: '#0F172A', fontWeight: 'bold', fontSize: 13 },
  txRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 12, borderBottomWidth: 1, borderColor: '#E2E8F0' },
  txDesc: { fontSize: 14, fontWeight: 'bold', color: '#0F172A' },
  txDate: { fontSize: 11, color: '#94A3B8', marginTop: 2 },
  txAmount: { fontSize: 14, fontWeight: 'bold' },
  sosAlertBox: { backgroundColor: '#FEF2F2', padding: 20, borderRadius: 16, borderWidth: 1, borderColor: '#FECACA', marginBottom: 16 },
  sosAlertTitle: { fontSize: 18, fontWeight: 'bold', color: '#991B1B' },
  sosAlertDesc: { fontSize: 13, color: '#B91C1C', marginTop: 6, lineHeight: 18 },
  sosBigTrigger: { backgroundColor: '#DC2626', padding: 16, borderRadius: 12, alignItems: 'center', marginTop: 14 },
  sosBigTriggerText: { color: '#FFFFFF', fontWeight: 'bold', fontSize: 15 },
  safetyFeatureRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#FFFFFF', padding: 14, borderRadius: 12, marginBottom: 8, borderWidth: 1, borderColor: '#E2E8F0' },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'center', padding: 20 },
  modalCard: { backgroundColor: '#FFFFFF', padding: 24, borderRadius: 20, alignItems: 'center' },
  modalTitle: { fontSize: 20, fontWeight: 'bold', color: '#0F172A' },
  modalSub: { fontSize: 13, color: '#64748B', marginTop: 4 },
  starsRow: { flexDirection: 'row', marginVertical: 16 }
});
