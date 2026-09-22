import React, { useState, useEffect } from 'react';
import io from 'socket.io-client';

const socket = io('http://localhost:4000');

type AdminTab = 'OVERVIEW' | 'LIVE_MAP' | 'KYC' | 'PRICING' | 'COUPONS' | 'SOS';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState<AdminTab>('OVERVIEW');
  const [metrics, setMetrics] = useState({
    totalCustomers: 142850,
    onlineCaptains: 342,
    activeRides: 48,
    activeParcels: 19,
    todayGmv: 284500,
    platformCommission: 56900,
    pendingKyc: 12,
    openSosCount: 0
  });

  // KYC Queue
  const [kycQueue, setKycQueue] = useState([
    { id: 'CAP-1042', name: 'Sunil Verma', phone: '+91 98765 43210', vehicle: 'Bajaj Pulsar 150', vehicleNo: 'KA-04-MJ-1290', status: 'PENDING', docs: ['Driving License', 'RC Book', 'Insurance', 'PUC'] },
    { id: 'CAP-1043', name: 'Manish Gowda', phone: '+91 97654 32109', vehicle: 'Honda Activa 6G', vehicleNo: 'KA-01-AB-8821', status: 'PENDING', docs: ['Driving License', 'RC Book'] },
    { id: 'CAP-1044', name: 'Arun Kumar', phone: '+91 91234 56780', vehicle: 'TVS Jupiter 125', vehicleNo: 'KA-51-EF-4321', status: 'PENDING', docs: ['Driving License', 'RC Book', 'Insurance', 'Aadhaar'] }
  ]);

  // Pricing Config State
  const [pricingConfig, setPricingConfig] = useState({
    bikeTaxiBase: 25,
    bikeTaxiPerKm: 10,
    autoBase: 30,
    autoPerKm: 14,
    parcelBase: 35,
    parcelPerKm: 11,
    surgeMultiplier: 1.3,
    nightChargeActive: true
  });

  // Coupons State
  const [coupons, setCoupons] = useState([
    { code: 'RAPIDO50', discount: '50% (Max ₹35)', minRide: '₹50', active: true, usageCount: 4120 },
    { code: 'FIRSTFREE', discount: 'Flat ₹40 Off', minRide: '₹40', active: true, usageCount: 1890 },
    { code: 'PARCEL20', discount: '20% Off', minRide: '₹60', active: true, usageCount: 780 }
  ]);
  const [newCouponCode, setNewCouponCode] = useState('');
  const [newCouponDiscount, setNewCouponDiscount] = useState('');

  // Live SOS Alerts
  const [liveSosAlerts, setLiveSosAlerts] = useState<any[]>([
    {
      alertId: 'SOS-9482',
      tripId: 'RIDE-9428',
      userId: 'CUST-001',
      role: 'CUSTOMER',
      lat: 12.9716,
      lng: 77.5946,
      timestamp: '2 mins ago',
      status: 'DISPATCHING_PATROL'
    }
  ]);

  useEffect(() => {
    socket.on('admin:sos_alert', (data) => {
      setLiveSosAlerts((prev) => [data, ...prev]);
      setMetrics((prev) => ({ ...prev, openSosCount: prev.openSosCount + 1 }));
    });

    socket.on('admin:ride_update', () => {
      setMetrics((prev) => ({ ...prev, activeRides: prev.activeRides + 1 }));
    });

    return () => {
      socket.off('admin:sos_alert');
      socket.off('admin:ride_update');
    };
  }, []);

  const handleApproveKyc = (id: string) => {
    setKycQueue(kycQueue.filter((item) => item.id !== id));
    setMetrics((prev) => ({
      ...prev,
      pendingKyc: Math.max(0, prev.pendingKyc - 1),
      onlineCaptains: prev.onlineCaptains + 1
    }));
  };

  const handleRejectKyc = (id: string) => {
    setKycQueue(kycQueue.filter((item) => item.id !== id));
    setMetrics((prev) => ({
      ...prev,
      pendingKyc: Math.max(0, prev.pendingKyc - 1)
    }));
  };

  const handleAddCoupon = () => {
    if (!newCouponCode) return;
    setCoupons([
      ...coupons,
      { code: newCouponCode.toUpperCase(), discount: newCouponDiscount || '20% Off', minRide: '₹50', active: true, usageCount: 0 }
    ]);
    setNewCouponCode('');
    setNewCouponDiscount('');
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'Inter, system-ui, sans-serif', backgroundColor: '#0f172a', color: '#f8fafc' }}>
      {/* 1. OPERATIONS SIDEBAR */}
      <div style={{ width: '280px', backgroundColor: '#1e293b', borderRight: '1px solid #334155', padding: '24px', display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '32px' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '10px', backgroundColor: '#facc15', color: '#0f172a', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '900', fontSize: '20px' }}>
            ⚡
          </div>
          <div>
            <h1 style={{ fontSize: '18px', fontWeight: 'bold', margin: 0 }}>VeloGo Ops</h1>
            <span style={{ fontSize: '12px', color: '#94a3b8' }}>City Operations Command</span>
          </div>
        </div>

        <nav style={{ display: 'flex', flexDirection: 'column', gap: '6px', flex: 1 }}>
          {[
            { key: 'OVERVIEW', label: '📊 Metrics Overview' },
            { key: 'LIVE_MAP', label: '🗺️ Fleet Telematics Map' },
            { key: 'KYC', label: `🪪 Captain KYC (${kycQueue.length})` },
            { key: 'PRICING', label: '🏷️ Pricing & Dynamic Surge' },
            { key: 'COUPONS', label: '🎟️ Coupons & Campaigns' },
            { key: 'SOS', label: `🚨 Safety & SOS Center (${liveSosAlerts.length})` }
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as AdminTab)}
              style={{
                textAlign: 'left',
                padding: '12px 16px',
                borderRadius: '10px',
                border: 'none',
                backgroundColor: activeTab === tab.key ? '#facc15' : 'transparent',
                color: activeTab === tab.key ? '#0f172a' : '#cbd5e1',
                fontWeight: 'bold',
                cursor: 'pointer',
                fontSize: '14px',
                transition: 'all 0.15s'
              }}
            >
              {tab.label}
            </button>
          ))}
        </nav>

        {/* System Status Pill */}
        <div style={{ backgroundColor: '#0f172a', padding: '14px', borderRadius: '12px', border: '1px solid #334155' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '4px', backgroundColor: '#10b981' }}></span>
            <span style={{ fontSize: '12px', color: '#cbd5e1', fontWeight: '600' }}>WebSocket Gateway Online</span>
          </div>
          <span style={{ fontSize: '11px', color: '#64748b', display: 'block', marginTop: '4px' }}>Bangalore Geofence • 4,200 req/min</span>
        </div>
      </div>

      {/* 2. MAIN DASHBOARD CONTENT */}
      <div style={{ flex: 1, padding: '32px', overflowY: 'auto' }}>
        {/* OVERVIEW TAB */}
        {activeTab === 'OVERVIEW' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <div>
                <h2 style={{ fontSize: '24px', fontWeight: 'bold', margin: 0 }}>Real-Time Operations Overview</h2>
                <p style={{ color: '#94a3b8', fontSize: '13px', margin: '4px 0 0' }}>Live telemetry synchronized from Customer & Captain applications</p>
              </div>
              <button 
                onClick={() => alert('Data refreshed from telemetry nodes')}
                style={{ backgroundColor: '#334155', color: '#fff', border: 'none', padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', fontSize: '13px', fontWeight: 'bold' }}
              >
                🔄 Refresh Telemetry
              </button>
            </div>

            {/* Metric KPI Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '28px' }}>
              <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' }}>
                <div style={{ fontSize: '12px', color: '#94a3b8', fontWeight: 'bold' }}>TODAY'S GMV</div>
                <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#facc15', marginTop: '6px' }}>₹{metrics.todayGmv.toLocaleString()}</div>
                <div style={{ fontSize: '11px', color: '#10b981', marginTop: '4px' }}>↑ +14.2% vs yesterday</div>
              </div>
              <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' }}>
                <div style={{ fontSize: '12px', color: '#94a3b8', fontWeight: 'bold' }}>ONLINE CAPTAINS</div>
                <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#10b981', marginTop: '6px' }}>{metrics.onlineCaptains}</div>
                <div style={{ fontSize: '11px', color: '#94a3b8', marginTop: '4px' }}>88% acceptance rate</div>
              </div>
              <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' }}>
                <div style={{ fontSize: '12px', color: '#94a3b8', fontWeight: 'bold' }}>ACTIVE TRIPS (RIDES & PARCELS)</div>
                <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#38bdf8', marginTop: '6px' }}>{metrics.activeRides + metrics.activeParcels}</div>
                <div style={{ fontSize: '11px', color: '#94a3b8', marginTop: '4px' }}>{metrics.activeRides} Rides • {metrics.activeParcels} Parcels</div>
              </div>
              <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' }}>
                <div style={{ fontSize: '12px', color: '#94a3b8', fontWeight: 'bold' }}>PLATFORM COMMISSION (20%)</div>
                <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#a855f7', marginTop: '6px' }}>₹{metrics.platformCommission.toLocaleString()}</div>
                <div style={{ fontSize: '11px', color: '#10b981', marginTop: '4px' }}>Net Revenue</div>
              </div>
            </div>

            {/* Live Operational Health */}
            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
              <div style={{ backgroundColor: '#1e293b', padding: '24px', borderRadius: '14px', border: '1px solid #334155' }}>
                <h3 style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '16px' }}>City Peak Demand Breakdown</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {[
                    { zone: 'Indiranagar 100ft Road', demand: 'High Surge (1.4x)', rides: '18 active' },
                    { zone: 'Koramangala 5th Block', demand: 'Moderate Surge (1.2x)', rides: '14 active' },
                    { zone: 'HSR Layout Sector 1', demand: 'Normal (1.0x)', rides: '9 active' },
                    { zone: 'Electronic City Phase 1', demand: 'High Surge (1.3x)', rides: '12 active' }
                  ].map((z, idx) => (
                    <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', backgroundColor: '#0f172a', borderRadius: '10px' }}>
                      <span style={{ fontWeight: 'bold', fontSize: '13px' }}>📍 {z.zone}</span>
                      <span style={{ color: '#facc15', fontSize: '12px', fontWeight: '600' }}>{z.demand}</span>
                      <span style={{ color: '#94a3b8', fontSize: '12px' }}>{z.rides}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div style={{ backgroundColor: '#1e293b', padding: '24px', borderRadius: '14px', border: '1px solid #334155' }}>
                <h3 style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '16px' }}>Dispatch Health</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                      <span style={{ color: '#94a3b8' }}>Average Captain ETA</span>
                      <span style={{ fontWeight: 'bold', color: '#10b981' }}>2.8 Mins</span>
                    </div>
                    <div style={{ height: '6px', backgroundColor: '#0f172a', borderRadius: '3px' }}>
                      <div style={{ width: '85%', height: '100%', backgroundColor: '#10b981', borderRadius: '3px' }}></div>
                    </div>
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                      <span style={{ color: '#94a3b8' }}>Dispatch Matching Speed</span>
                      <span style={{ fontWeight: 'bold', color: '#38bdf8' }}>1.4 Seconds</span>
                    </div>
                    <div style={{ height: '6px', backgroundColor: '#0f172a', borderRadius: '3px' }}>
                      <div style={{ width: '92%', height: '100%', backgroundColor: '#38bdf8', borderRadius: '3px' }}></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* LIVE MAP TAB */}
        {activeTab === 'LIVE_MAP' && (
          <div>
            <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px' }}>Geospatial Fleet Telematics Radar</h2>
            <p style={{ color: '#94a3b8', fontSize: '13px', marginBottom: '20px' }}>Tracking active driver telemetry, trip vectors, and high-demand geofences.</p>
            
            <div style={{ height: '520px', backgroundColor: '#1e293b', borderRadius: '16px', border: '1px solid #334155', position: 'relative', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <div style={{ position: 'absolute', top: '16px', left: '16px', backgroundColor: '#0f172a', padding: '12px 18px', borderRadius: '12px', border: '1px solid #334155' }}>
                <span style={{ fontSize: '12px', color: '#10b981', fontWeight: 'bold' }}>🟢 342 CAPTAINS TRANSMITTING GPS</span>
              </div>

              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '56px', marginBottom: '16px' }}>🛰️</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold' }}>Bangalore Metro Central Radar Active</div>
                <div style={{ fontSize: '13px', color: '#94a3b8', marginTop: '6px' }}>Geofence: 12.9716° N, 77.5946° E • Real-Time Packet Stream</div>
              </div>
            </div>
          </div>
        )}

        {/* KYC VERIFICATION QUEUE TAB */}
        {activeTab === 'KYC' && (
          <div>
            <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px' }}>Captain KYC Verification Queue</h2>
            <p style={{ color: '#94a3b8', fontSize: '13px', marginBottom: '20px' }}>Review and verify submitted Driver Licenses, RC Books, and Police Verification.</p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {kycQueue.map((cap) => (
                <div key={cap.id} style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ fontSize: '17px', fontWeight: 'bold' }}>{cap.name} <span style={{ color: '#facc15', fontSize: '13px' }}>({cap.id})</span></div>
                    <div style={{ fontSize: '13px', color: '#94a3b8', marginTop: '4px' }}>Phone: {cap.phone} • Vehicle: {cap.vehicle} ({cap.vehicleNo})</div>
                    <div style={{ display: 'flex', gap: '8px', marginTop: '10px' }}>
                      {cap.docs.map((d, i) => (
                        <span key={i} style={{ fontSize: '12px', backgroundColor: '#0f172a', padding: '4px 10px', borderRadius: '6px', border: '1px solid #334155' }}>📄 {d}</span>
                      ))}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button onClick={() => handleRejectKyc(cap.id)} style={{ backgroundColor: '#ef4444', color: '#fff', border: 'none', padding: '10px 18px', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', fontSize: '13px' }}>Reject</button>
                    <button onClick={() => handleApproveKyc(cap.id)} style={{ backgroundColor: '#10b981', color: '#fff', border: 'none', padding: '10px 18px', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', fontSize: '13px' }}>Approve Captain</button>
                  </div>
                </div>
              ))}
              {kycQueue.length === 0 && (
                <div style={{ padding: '40px', textAlign: 'center', backgroundColor: '#1e293b', borderRadius: '14px' }}>
                  <span style={{ fontSize: '32px' }}>✅</span>
                  <p style={{ marginTop: '10px', color: '#94a3b8' }}>All pending KYC applications have been reviewed!</p>
                </div>
              )}
            </div>
          </div>
        )}

        {/* PRICING & SURGE CONFIG TAB */}
        {activeTab === 'PRICING' && (
          <div>
            <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px' }}>Pricing & Surge Multipliers</h2>
            <p style={{ color: '#94a3b8', fontSize: '13px', marginBottom: '24px' }}>Configure city base fares, distance per-km rates, and real-time surge triggers.</p>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px', marginBottom: '28px' }}>
              <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' }}>
                <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#facc15', marginBottom: '14px' }}>🛵 Bike Taxi Pricing</h3>
                <div style={{ marginBottom: '12px' }}>
                  <label style={{ fontSize: '12px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Base Fare (₹)</label>
                  <input 
                    type="number" 
                    value={pricingConfig.bikeTaxiBase} 
                    onChange={(e) => setPricingConfig({ ...pricingConfig, bikeTaxiBase: parseInt(e.target.value) || 0 })}
                    style={{ width: '100%', padding: '10px', borderRadius: '8px', backgroundColor: '#0f172a', border: '1px solid #334155', color: '#fff' }} 
                  />
                </div>
                <div>
                  <label style={{ fontSize: '12px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Per Km Rate (₹)</label>
                  <input 
                    type="number" 
                    value={pricingConfig.bikeTaxiPerKm} 
                    onChange={(e) => setPricingConfig({ ...pricingConfig, bikeTaxiPerKm: parseInt(e.target.value) || 0 })}
                    style={{ width: '100%', padding: '10px', borderRadius: '8px', backgroundColor: '#0f172a', border: '1px solid #334155', color: '#fff' }} 
                  />
                </div>
              </div>

              <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' }}>
                <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#38bdf8', marginBottom: '14px' }}>🛺 Auto Pricing</h3>
                <div style={{ marginBottom: '12px' }}>
                  <label style={{ fontSize: '12px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Base Fare (₹)</label>
                  <input 
                    type="number" 
                    value={pricingConfig.autoBase} 
                    onChange={(e) => setPricingConfig({ ...pricingConfig, autoBase: parseInt(e.target.value) || 0 })}
                    style={{ width: '100%', padding: '10px', borderRadius: '8px', backgroundColor: '#0f172a', border: '1px solid #334155', color: '#fff' }} 
                  />
                </div>
                <div>
                  <label style={{ fontSize: '12px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Per Km Rate (₹)</label>
                  <input 
                    type="number" 
                    value={pricingConfig.autoPerKm} 
                    onChange={(e) => setPricingConfig({ ...pricingConfig, autoPerKm: parseInt(e.target.value) || 0 })}
                    style={{ width: '100%', padding: '10px', borderRadius: '8px', backgroundColor: '#0f172a', border: '1px solid #334155', color: '#fff' }} 
                  />
                </div>
              </div>

              <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' }}>
                <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#10b981', marginBottom: '14px' }}>📦 Parcel Express</h3>
                <div style={{ marginBottom: '12px' }}>
                  <label style={{ fontSize: '12px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Base Fare (₹)</label>
                  <input 
                    type="number" 
                    value={pricingConfig.parcelBase} 
                    onChange={(e) => setPricingConfig({ ...pricingConfig, parcelBase: parseInt(e.target.value) || 0 })}
                    style={{ width: '100%', padding: '10px', borderRadius: '8px', backgroundColor: '#0f172a', border: '1px solid #334155', color: '#fff' }} 
                  />
                </div>
                <div>
                  <label style={{ fontSize: '12px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Per Km Rate (₹)</label>
                  <input 
                    type="number" 
                    value={pricingConfig.parcelPerKm} 
                    onChange={(e) => setPricingConfig({ ...pricingConfig, parcelPerKm: parseInt(e.target.value) || 0 })}
                    style={{ width: '100%', padding: '10px', borderRadius: '8px', backgroundColor: '#0f172a', border: '1px solid #334155', color: '#fff' }} 
                  />
                </div>
              </div>
            </div>

            <button 
              onClick={() => alert('Pricing rules successfully deployed across Bangalore Geofence!')}
              style={{ backgroundColor: '#facc15', color: '#0f172a', border: 'none', padding: '14px 28px', borderRadius: '10px', fontWeight: 'bold', fontSize: '15px', cursor: 'pointer' }}
            >
              DEPLOY PRICING UPDATES
            </button>
          </div>
        )}

        {/* COUPONS TAB */}
        {activeTab === 'COUPONS' && (
          <div>
            <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px' }}>Coupons & Promotions Engine</h2>
            <p style={{ color: '#94a3b8', fontSize: '13px', marginBottom: '24px' }}>Create promo codes and track redemption rates across user segments.</p>

            <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155', marginBottom: '24px', display: 'flex', gap: '14px', alignItems: 'flex-end' }}>
              <div style={{ flex: 1 }}>
                <label style={{ fontSize: '12px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Promo Code</label>
                <input 
                  type="text" 
                  placeholder="e.g. MONSOON30" 
                  value={newCouponCode}
                  onChange={(e) => setNewCouponCode(e.target.value)}
                  style={{ width: '100%', padding: '10px', borderRadius: '8px', backgroundColor: '#0f172a', border: '1px solid #334155', color: '#fff' }} 
                />
              </div>
              <div style={{ flex: 1 }}>
                <label style={{ fontSize: '12px', color: '#94a3b8', display: 'block', marginBottom: '4px' }}>Discount Value</label>
                <input 
                  type="text" 
                  placeholder="e.g. 30% Off" 
                  value={newCouponDiscount}
                  onChange={(e) => setNewCouponDiscount(e.target.value)}
                  style={{ width: '100%', padding: '10px', borderRadius: '8px', backgroundColor: '#0f172a', border: '1px solid #334155', color: '#fff' }} 
                />
              </div>
              <button 
                onClick={handleAddCoupon}
                style={{ backgroundColor: '#10b981', color: '#fff', border: 'none', padding: '10px 20px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer', height: '42px' }}
              >
                + Create Coupon
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
              {coupons.map((c, i) => (
                <div key={i} style={{ backgroundColor: '#1e293b', padding: '18px', borderRadius: '12px', border: '1px solid #334155' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                    <span style={{ backgroundColor: '#facc15', color: '#0f172a', fontWeight: 'bold', fontSize: '13px', padding: '4px 8px', borderRadius: '6px' }}>{c.code}</span>
                    <span style={{ color: '#10b981', fontSize: '12px', fontWeight: 'bold' }}>🟢 Active</span>
                  </div>
                  <div style={{ fontSize: '16px', fontWeight: 'bold' }}>{c.discount}</div>
                  <div style={{ fontSize: '12px', color: '#94a3b8', marginTop: '4px' }}>Min Order: {c.minRide} • {c.usageCount} Redemptions</div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* SOS EMERGENCY CENTER TAB */}
        {activeTab === 'SOS' && (
          <div>
            <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px', color: '#ef4444' }}>🚨 24x7 Emergency Safety Command Center</h2>
            <p style={{ color: '#94a3b8', fontSize: '13px', marginBottom: '20px' }}>Real-time listener for panic button events from Riders and Captains.</p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {liveSosAlerts.map((sos, i) => (
                <div key={i} style={{ backgroundColor: '#7f1d1d33', padding: '20px', borderRadius: '14px', border: '1.5px solid #ef4444', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <span style={{ backgroundColor: '#ef4444', color: '#fff', fontSize: '12px', fontWeight: 'bold', padding: '3px 8px', borderRadius: '4px' }}>EMERGENCY ACTIVE</span>
                      <span style={{ fontSize: '15px', fontWeight: 'bold' }}>{sos.alertId} • Trip: {sos.tripId}</span>
                    </div>
                    <div style={{ fontSize: '13px', color: '#fca5a5', marginTop: '6px' }}>
                      Triggered by: {sos.role} ({sos.userId}) • GPS: {sos.lat}, {sos.lng}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button 
                      onClick={() => alert(`Calling Police Patrol Dispatch for coordinates: ${sos.lat}, ${sos.lng}`)}
                      style={{ backgroundColor: '#ef4444', color: '#fff', border: 'none', padding: '10px 18px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
                    >
                      🚔 Dispatch Police Patrol
                    </button>
                    <button 
                      onClick={() => alert('Customer & Captain audio bridge connected')}
                      style={{ backgroundColor: '#334155', color: '#fff', border: 'none', padding: '10px 18px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
                    >
                      📞 Bridge Call
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
