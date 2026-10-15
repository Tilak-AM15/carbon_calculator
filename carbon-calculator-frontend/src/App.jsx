import React, { useEffect, useMemo, useState } from 'react'
import {
  Activity, ArrowDownRight, ArrowUpRight, Award, BarChart3, Bell, Check,
  ChevronDown, ChevronRight, CircleAlert, CircleCheck, CircleHelp, Compass,
  Database, Download, ExternalLink, Eye, Factory, FileText, Filter, Globe,
  HelpCircle, Info, Layers, Leaf, Lock, LogOut, Menu, MoreHorizontal,
  Package, Plus, RefreshCw, Search, Shield, ShieldAlert, ShieldCheck,
  Sparkles, Split, Sprout, Tag, Truck, UserCheck, Users, Warehouse, X, Zap
} from 'lucide-react'

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// Demo accounts for instant role-based testing
const DEMO_ACCOUNTS = [
  { username: 'admin', password: 'AdminDemo123!', role: 'ADMIN', label: 'Platform Admin', org: 'Evergreen Platform Administration', badge: 'Full Admin' },
  { username: 'supplier', password: 'SupplierDemo123!', role: 'STAKEHOLDER', label: 'Raw Material Supplier', org: 'Apex Sustainable Agriculture', badge: 'Supplier Org' },
  { username: 'logistics', password: 'SupplierDemo123!', role: 'STAKEHOLDER', label: 'Green Freight Logistics', org: 'TransGlobal Green Freight', badge: 'Logistics Org' },
  { username: 'manufacturer', password: 'SupplierDemo123!', role: 'STAKEHOLDER', label: 'Precision Manufacturer', org: 'EcoTech Precision Manufacturing', badge: 'Manufacturer Org' },
  { username: 'distributor', password: 'SupplierDemo123!', role: 'STAKEHOLDER', label: 'Regional Distributor', org: 'Central Green Distribution', badge: 'Distributor Org' },
  { username: 'wholesaler', password: 'SupplierDemo123!', role: 'STAKEHOLDER', label: 'Wholesale Aggregator', org: 'Nordic Wholesalers Network', badge: 'Wholesaler Org' },
  { username: 'retailer', password: 'SupplierDemo123!', role: 'STAKEHOLDER', label: 'Sustainable Retailer', org: 'BioMarket Retail Stores', badge: 'Retailer Org' },
  { username: 'wastehandler', password: 'SupplierDemo123!', role: 'STAKEHOLDER', label: 'Circular Waste Recycler', org: 'Circular Cycle Reclamation', badge: 'Waste Handler Org' },
  { username: 'reviewer', password: 'ReviewerDemo123!', role: 'REVIEWER', label: 'Independent Auditor', org: 'EcoAudit International', badge: 'Read-Only Auditor' },
]

const initialForm = {
  organizationName: 'Apex Sustainable Agriculture',
  facilityCode: 'FAC-APEX-FARM',
  batchId: 'BATCH-2026-ECO-01',
  stageSequence: 1,
  stageName: '1. Raw Material Sourcing',
  activityName: 'Additional Feedstock Sourcing',
  scope: 'SCOPE_3',
  categoryCode: 'CAT_1',
  quantity: '500',
  unit: 'kg',
  factorId: '',
  allocationNumerator: '100',
  allocationDenominator: '100',
  shippedMassTonnes: '',
  distanceKm: '',
  gasSpecificName: '',
  isScope2MarketBased: false,
  isBatchProductAllocated: true,
  lifecycleStage: 'CRADLE_TO_RETAIL',
  eventKey: '',
  transferLegIdentifier: '',
  methodNote: 'Activity-based measurement with certified supplier receipt.',
  assumptionsNote: 'Allocated 100% to production batch.'
}

const fmt = (n, digits = 2) => Number(n || 0).toLocaleString(undefined, { maximumFractionDigits: digits, minimumFractionDigits: 0 })

function Logo() {
  return (
    <div className="flex items-center gap-3">
      <div className="grid h-10 w-10 place-items-center rounded-2xl bg-gradient-to-br from-[#d4f3dd] to-[#aee4be] text-[#1c5f3b] shadow-sm">
        <Leaf size={21} className="stroke-[2.3]" />
      </div>
      <div>
        <p className="font-display text-[16px] font-extrabold tracking-tight text-[#163828]">
          evergreen<span className="text-[#3ea468]">.</span>
        </p>
        <p className="text-[10px] font-bold uppercase tracking-[.2em] text-[#7a8f82]">
          Carbon Intelligence
        </p>
      </div>
    </div>
  )
}

function Metric({ icon: Icon, label, value, unit, trend, tone = 'green', subtitle }) {
  const tones = {
    green: 'bg-[#e2f4e7] text-[#256f43] border-[#c8e8d0]',
    blue: 'bg-[#e7efff] text-[#3462ad] border-[#cfdffc]',
    amber: 'bg-[#fff0db] text-[#9b6118] border-[#fde1bc]',
    violet: 'bg-[#f0eaff] text-[#6d4bb5] border-[#dfd2fb]',
    emerald: 'bg-[#daf6e4] text-[#1e613b] border-[#bbebcd]',
  }
  return (
    <div className="rounded-[22px] border border-[#e6ece5] bg-white p-5 shadow-sm transition-all hover:shadow-md">
      <div className="flex items-start justify-between">
        <div className={`grid h-11 w-11 place-items-center rounded-2xl border ${tones[tone]}`}>
          <Icon size={20} />
        </div>
        {trend && (
          <span className="flex items-center gap-1 rounded-full bg-[#ecf7ef] px-2.5 py-1 text-[11px] font-bold text-[#2a7a4b]">
            <ArrowUpRight size={13} />{trend}
          </span>
        )}
      </div>
      <p className="mt-4 text-[11px] font-bold uppercase tracking-wider text-[#7e8e83]">{label}</p>
      <div className="mt-1 flex items-baseline gap-2">
        <span className="font-display text-[26px] font-extrabold tracking-tight text-[#19382a] sm:text-[30px]">{value}</span>
        <span className="text-xs font-semibold text-[#7c8d82]">{unit}</span>
      </div>
      {subtitle && <p className="mt-1 text-[11px] text-[#8e9f94]">{subtitle}</p>}
    </div>
  )
}

function Badge({ children, color = 'green' }) {
  const c = {
    green: 'bg-[#e7f6eb] text-[#256c42] border border-[#cbe8d2]',
    blue: 'bg-[#e9f1ff] text-[#3965b3] border border-[#cee0fc]',
    amber: 'bg-[#fff2e0] text-[#9c631b] border border-[#fae2c1]',
    gray: 'bg-[#f1f4f1] text-[#65756b] border border-[#e1e6e2]',
    purple: 'bg-[#f3edff] text-[#6b47b8] border border-[#e2d5fc]',
    red: 'bg-[#fdeeee] text-[#b83b3b] border border-[#fad4d4]',
  }
  return (
    <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-[10px] font-bold ${c[color]}`}>
      {children}
    </span>
  )
}

export default function App() {
  const [username, setUsername] = useState('supplier')
  const [password, setPassword] = useState('SupplierDemo123!')
  const [auth, setAuth] = useState('')
  const [me, setMe] = useState(null)

  // Core Data State
  const [categories, setCategories] = useState([])
  const [categoryStatusList, setCategoryStatusList] = useState([])
  const [factors, setFactors] = useState([])
  const [activities, setActivities] = useState([])
  const [organizations, setOrganizations] = useState([])
  const [facilities, setFacilities] = useState([])
  const [transfers, setTransfers] = useState([])
  const [summary, setSummary] = useState(null)
  const [inventoryReport, setInventoryReport] = useState(null)
  const [auditLogs, setAuditLogs] = useState([])

  // UI State
  const [currentBatchId, setCurrentBatchId] = useState('BATCH-2026-ECO-01')
  const [activeNav, setActiveNav] = useState('Overview')
  const [mobileNav, setMobileNav] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [selectedFactorForModal, setSelectedFactorForModal] = useState(null)
  const [factorDetailModal, setFactorDetailModal] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [busy, setBusy] = useState(false)
  const [search, setSearch] = useState('')
  const [scopeFilter, setScopeFilter] = useState('ALL')
  const [scope2Mode, setScope2Mode] = useState('LOCATION') // 'LOCATION' vs 'MARKET'

  // Activity Form State
  const [form, setForm] = useState(initialForm)

  const headers = useMemo(() => ({
    'Content-Type': 'application/json',
    ...(auth ? { Authorization: `Basic ${auth}` } : {})
  }), [auth])

  async function api(path, options = {}) {
    const res = await fetch(`${API}${path}`, {
      ...options,
      headers: { ...headers, ...(options.headers || {}) }
    })
    if (!res.ok) {
      let msg = `${res.status} ${res.statusText}`
      try {
        const body = await res.json()
        msg = body.error || body.message || msg
        if (body.details && Array.isArray(body.details)) {
          msg += ' — ' + body.details.join(', ')
        }
      } catch {}
      throw new Error(msg)
    }
    return res.status === 204 ? null : res.json()
  }

  async function refresh(currentAuth = auth) {
    if (!currentAuth) return
    setError('')
    try {
      const [u, c, cs, f, a, orgs, facs, bSum, inv, tr, aud] = await Promise.all([
        api('/api/me'),
        api('/api/categories'),
        api('/api/category-status'),
        api('/api/factors'),
        api('/api/activities'),
        api('/api/organizations'),
        api('/api/facilities'),
        api(`/api/batches/${encodeURIComponent(currentBatchId)}/summary`),
        api('/api/reports/organization-inventory'),
        api(`/api/transfers?batchId=${encodeURIComponent(currentBatchId)}`),
        api('/api/audit')
      ])
      setMe(u)
      setCategories(c)
      setCategoryStatusList(cs)
      setFactors(f)
      setActivities(a)
      setOrganizations(orgs)
      setFacilities(facs)
      setSummary(bSum)
      setInventoryReport(inv)
      setTransfers(tr)
      setAuditLogs(aud)

      // Set form defaults based on user role
      setForm(prev => ({
        ...prev,
        organizationName: u.role === 'ADMIN' ? prev.organizationName : u.organizationName,
        factorId: prev.factorId || String(f[0]?.id || '')
      }))
    } catch (e) {
      setError(e.message)
    }
  }

  async function handleLogin(e, customUser, customPass) {
    if (e) e.preventDefault()
    setBusy(true)
    setError('')
    const u = customUser || username
    const p = customPass || password
    const token = btoa(`${u}:${p}`)

    try {
      const userProfile = await fetch(`${API}/api/me`, {
        headers: { Authorization: `Basic ${token}` }
      }).then(r => {
        if (!r.ok) throw new Error(r.status === 401 ? 'Invalid username or password' : `Login failed (${r.status})`)
        return r.json()
      })

      setAuth(token)
      setUsername(u)
      setPassword(p)
      setMe(userProfile)
      setNotice(`Signed in as ${userProfile.username} (${userProfile.role} · ${userProfile.organizationName})`)
      await refresh(token)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  function handleQuickSwitch(acc) {
    setUsername(acc.username)
    setPassword(acc.password)
    handleLogin(null, acc.username, acc.password)
  }

  function logout() {
    setAuth('')
    setMe(null)
    setSummary(null)
    setActivities([])
    setAuditLogs([])
  }

  useEffect(() => {
    if (auth) refresh()
  }, [auth, currentBatchId])

  // Live Unit Conversion Preview in Modal Form
  const selectedFactor = useMemo(() => {
    return factors.find(f => String(f.id) === String(form.factorId))
  }, [factors, form.factorId])

  const calculatedPreview = useMemo(() => {
    if (!selectedFactor) return null
    const qty = Number(form.quantity) || 0
    const num = Number(form.allocationNumerator) || 1
    const denom = Number(form.allocationDenominator) || 1
    const alloc = denom > 0 ? num / denom : 1
    const factorVal = Number(selectedFactor.factorValue) || 0

    let base = qty
    if (form.shippedMassTonnes && form.distanceKm && selectedFactor.activityUnit.includes('tonne.km')) {
      base = Number(form.shippedMassTonnes) * Number(form.distanceKm)
    }
    const rawKg = base * factorVal
    const allocKg = rawKg * alloc
    return {
      rawKg,
      allocKg,
      allocPct: (alloc * 100).toFixed(2),
      standardUnit: selectedFactor.activityUnit
    }
  }, [form, selectedFactor])

  async function createActivity(e) {
    e.preventDefault()
    setError('')
    setNotice('')
    try {
      const payload = {
        organizationName: form.organizationName,
        facilityCode: form.facilityCode || null,
        batchId: form.batchId,
        stageSequence: Number(form.stageSequence) || 1,
        stageName: form.stageName,
        activityName: form.activityName,
        scope: form.scope,
        categoryCode: form.categoryCode,
        quantity: Number(form.quantity),
        unit: form.unit,
        factorId: Number(form.factorId),
        allocationNumerator: Number(form.allocationNumerator),
        allocationDenominator: Number(form.allocationDenominator),
        shippedMassTonnes: form.shippedMassTonnes ? Number(form.shippedMassTonnes) : null,
        distanceKm: form.distanceKm ? Number(form.distanceKm) : null,
        gasSpecificName: form.gasSpecificName || null,
        isScope2MarketBased: Boolean(form.isScope2MarketBased),
        isBatchProductAllocated: Boolean(form.isBatchProductAllocated),
        lifecycleStage: form.lifecycleStage,
        eventKey: form.eventKey ? form.eventKey.trim() : null,
        transferLegIdentifier: form.transferLegIdentifier || null,
        methodNote: form.methodNote,
        assumptionsNote: form.assumptionsNote
      }

      await api('/api/activities', {
        method: 'POST',
        body: JSON.stringify(payload)
      })

      setNotice('Activity saved successfully. Backend decimal engine has snapshotted factor provenance and emissions.')
      setShowForm(false)
      await refresh()
    } catch (e) {
      setError(e.message)
    }
  }

  // Summary Metrics
  const totalReconciledKg = summary?.totalProductFootprintKgCo2e || 0
  const totalReconciledTonnes = summary?.totalProductFootprintTonnesCo2e || 0
  const intensityPerUnit = summary?.intensityPerUnitKgCo2e || 0
  const cradleToRetailKg = summary?.cradleToRetailKgCo2e || 0
  const useAndEolKg = summary?.useAndEndOfLifeKgCo2e || 0
  const scope1Kg = summary?.scope1KgCo2e || 0
  const scope2LocKg = summary?.scope2LocationBasedKgCo2e || 0
  const scope2MktKg = summary?.scope2MarketBasedKgCo2e || 0
  const scope3Kg = summary?.scope3KgCo2e || 0

  // Filtered Activities
  const filteredActivities = activities.filter(a => {
    const matchesSearch = `${a.activityName} ${a.stageName} ${a.organizationName} ${a.batchId} ${a.categoryCode} ${a.scope}`.toLowerCase().includes(search.toLowerCase())
    const matchesScope = scopeFilter === 'ALL' || a.scope === scopeFilter
    return matchesSearch && matchesScope
  })

  // Sign-in Screen
  if (!auth) {
    return (
      <div className="grid min-h-screen bg-[#f3f7f2] lg:grid-cols-2">
        {/* Left Branding Hero */}
        <div className="relative hidden overflow-hidden bg-gradient-to-br from-[#123826] via-[#184631] to-[#0e2c1e] p-12 text-white lg:flex lg:flex-col lg:justify-between">
          <div className="absolute -right-28 -top-24 h-[460px] w-[460px] rounded-full border border-white/10" />
          <div className="absolute -right-8 top-12 h-[300px] w-[300px] rounded-full border border-white/[.07]" />
          <div className="absolute -bottom-48 -left-20 h-[480px] w-[480px] rounded-full bg-[#377c52]/30 blur-3xl" />
          
          <Logo />

          <div className="relative z-10 max-w-xl">
            <Badge color="green">PRODUCT CARBON ACCOUNTING · POSTGRESQL</Badge>
            <h1 className="mt-7 font-display text-5xl font-extrabold leading-[1.12] tracking-tight text-white">
              Trust in every <span className="text-[#a4e6b5]">gram of carbon.</span>
            </h1>
            <p className="mt-5 max-w-lg text-[15px] leading-7 text-[#cbe0d3]">
              Track Scope 1, Scope 2, and all 15 Scope 3 emissions across your multi-tier supply chain. Reconcile verified product footprints with Climatiq emission factors and strict double-counting prevention.
            </p>

            <div className="mt-10 grid grid-cols-3 gap-3.5">
              {[
                ['01', 'Traceability', '9-stage supply chain journey'],
                ['02', 'Provenance', 'Climatiq-cited factors & GWP'],
                ['03', 'Integrity', 'RBAC & zero double counting']
              ].map(([num, title, desc]) => (
                <div key={num} className="rounded-2xl border border-white/10 bg-white/[.06] p-4 backdrop-blur-md">
                  <p className="text-xs font-bold text-[#a4e6b5]">{num}</p>
                  <p className="mt-2 text-sm font-bold text-white">{title}</p>
                  <p className="mt-1 text-[11px] leading-4 text-[#bfd6c7]">{desc}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="relative z-10 flex items-center justify-between border-t border-white/10 pt-5 text-xs text-[#a6c1b0]">
            <span>PostgreSQL · Spring Boot 3.4 · React + Vite</span>
            <span className="flex items-center gap-1.5"><ShieldCheck size={14} /> ISO 14067 & GHG Protocol Aligned</span>
          </div>
        </div>

        {/* Right Sign-in Form */}
        <div className="flex flex-col justify-center p-6 sm:p-12">
          <div className="mx-auto w-full max-w-[480px]">
            <div className="mb-8 lg:hidden">
              <Logo />
            </div>

            <div className="mb-8">
              <Badge color="green">DEMO ENVIRONMENT READY</Badge>
              <h2 className="mt-3 font-display text-3xl font-extrabold tracking-tight text-[#163828]">
                Sign in to your workspace
              </h2>
              <p className="mt-2 text-sm leading-6 text-[#728578]">
                Select any seeded demo stakeholder account or enter credentials manually.
              </p>
            </div>

            {error && (
              <div className="mb-5 flex items-center justify-between rounded-xl border border-red-200 bg-red-50 p-4 text-xs font-semibold text-red-700">
                <span>{error}</span>
                <button onClick={() => setError('')}><X size={15} /></button>
              </div>
            )}

            <form onSubmit={e => handleLogin(e)} className="space-y-4">
              <label className="block text-xs font-bold text-[#4d6355]">
                Username
                <input
                  className="mt-1.5"
                  autoComplete="username"
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  placeholder="Enter username"
                  required
                />
              </label>

              <label className="block text-xs font-bold text-[#4d6355]">
                Password
                <input
                  className="mt-1.5"
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  placeholder="Enter password"
                  required
                />
              </label>

              <button
                type="submit"
                disabled={busy}
                className="flex w-full items-center justify-center gap-2 rounded-xl bg-[#1b5537] py-3.5 text-sm font-bold text-white shadow-lg shadow-green-950/15 hover:bg-[#15462c] disabled:opacity-60"
              >
                {busy ? 'Authenticating…' : 'Sign in to Dashboard'}
                <ArrowUpRight size={16} />
              </button>
            </form>

            {/* Quick Demo Switcher Grid */}
            <div className="mt-7 rounded-2xl border border-[#e1eae1] bg-white p-4 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-[11px] font-extrabold uppercase tracking-wider text-[#63776b]">
                  Instant Role-Based Access Demo
                </p>
                <span className="text-[10px] text-[#93a297]">Click to test RBAC</span>
              </div>
              <p className="mt-1 text-[11px] text-[#819286]">
                Each account tests backend data isolation and permissions:
              </p>

              <div className="mt-3.5 grid grid-cols-2 gap-2 sm:grid-cols-3">
                {DEMO_ACCOUNTS.map(acc => (
                  <button
                    key={acc.username}
                    type="button"
                    onClick={() => handleQuickSwitch(acc)}
                    className="flex flex-col items-start rounded-xl border border-[#e8eee7] bg-[#f9faf8] p-2.5 text-left transition hover:border-[#429a6a] hover:bg-[#eff7f1]"
                  >
                    <span className="text-[11px] font-bold text-[#1f4731]">{acc.label}</span>
                    <span className="mt-0.5 text-[10px] text-[#718578]">{acc.username}</span>
                    <span className="mt-1.5 inline-block rounded bg-[#e8f4eb] px-1.5 py-0.5 text-[9px] font-extrabold text-[#297147]">
                      {acc.badge}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            <p className="mt-6 text-center text-xs text-[#95a49a]">
              Evergreen Carbon Intelligence · PostgreSQL Relational Prototype
            </p>
          </div>
        </div>
      </div>
    )
  }

  // Authenticated Application
  return (
    <div className="min-h-screen bg-[#f4f7f3] text-[#163828]">
      {/* Sidebar Navigation */}
      <aside className={`fixed inset-y-0 left-0 z-40 flex w-[260px] flex-col border-r border-[#e3eae2] bg-white px-5 py-6 transition-transform lg:translate-x-0 ${mobileNav ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="flex items-center justify-between">
          <Logo />
          <button className="rounded-lg p-2 lg:hidden text-[#67796d]" onClick={() => setMobileNav(false)}>
            <X size={18} />
          </button>
        </div>

        {/* User Role Badge in Sidebar */}
        <div className="mt-6 rounded-2xl border border-[#e5ece4] bg-[#f8faf8] p-3.5">
          <div className="flex items-center gap-2.5">
            <div className="grid h-9 w-9 place-items-center rounded-xl bg-[#dcf1e1] text-xs font-extrabold text-[#22673f]">
              {username.slice(0, 2).toUpperCase()}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-xs font-bold text-[#193a2b]">{username}</p>
              <p className="truncate text-[10px] font-semibold text-[#6d8174]">{me?.role || 'STAKEHOLDER'}</p>
            </div>
            <Badge color={me?.role === 'ADMIN' ? 'purple' : me?.role === 'REVIEWER' ? 'blue' : 'green'}>
              {me?.role === 'REVIEWER' ? 'Auditor' : me?.stakeholderRole || 'User'}
            </Badge>
          </div>
          <p className="mt-2 truncate text-[10px] text-[#7d9083]">
            {me?.organizationName}
          </p>
        </div>

        {/* Navigation Items */}
        <div className="mt-7">
          <p className="mb-2 px-3 text-[10px] font-extrabold uppercase tracking-[.2em] text-[#93a498]">WORKSPACE</p>
          {[
            { name: 'Overview', icon: Activity },
            { name: 'Product journey', icon: Truck },
            { name: 'Activity ledger', icon: Package, count: activities.length },
            { name: 'Scope 3 matrix', icon: Layers, count: 15 },
          ].map(({ name, icon: Icon, count }) => (
            <button
              key={name}
              onClick={() => { setActiveNav(name); setMobileNav(false) }}
              className={`mb-1 flex w-full items-center gap-3 rounded-xl px-3.5 py-3 text-left text-[13px] font-semibold transition ${
                activeNav === name
                  ? 'bg-[#e7f5ec] text-[#206b3e] shadow-xs'
                  : 'text-[#6e8174] hover:bg-[#f6f8f5] hover:text-[#254d37]'
              }`}
            >
              <Icon size={17} />
              <span className="flex-1">{name}</span>
              {count !== undefined && (
                <span className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${activeNav === name ? 'bg-[#cbe8d3] text-[#1b5e36]' : 'bg-[#eef2ee] text-[#718578]'}`}>
                  {count}
                </span>
              )}
            </button>
          ))}
        </div>

        <div className="mt-6">
          <p className="mb-2 px-3 text-[10px] font-extrabold uppercase tracking-[.2em] text-[#93a498]">PROVENANCE & AUDIT</p>
          {[
            { name: 'Climatiq factors', icon: Zap, count: factors.length },
            { name: 'Double counting report', icon: Split },
            { name: 'Audit trail', icon: ShieldCheck, count: auditLogs.length },
          ].map(({ name, icon: Icon, count }) => (
            <button
              key={name}
              onClick={() => { setActiveNav(name); setMobileNav(false) }}
              className={`mb-1 flex w-full items-center gap-3 rounded-xl px-3.5 py-3 text-left text-[13px] font-semibold transition ${
                activeNav === name
                  ? 'bg-[#e7f5ec] text-[#206b3e] shadow-xs'
                  : 'text-[#6e8174] hover:bg-[#f6f8f5] hover:text-[#254d37]'
              }`}
            >
              <Icon size={17} />
              <span className="flex-1">{name}</span>
              {count !== undefined && (
                <span className="rounded-full bg-[#eef2ee] px-2 py-0.5 text-[10px] font-bold text-[#718578]">
                  {count}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Quick Role Switcher Panel in Sidebar */}
        <div className="mt-auto rounded-2xl border border-[#e4ede3] bg-[#f7faf7] p-3">
          <p className="text-[10px] font-extrabold uppercase tracking-wider text-[#6b7e71]">Quick Role Switcher</p>
          <div className="mt-2 grid grid-cols-3 gap-1">
            {['admin', 'supplier', 'logistics', 'manufacturer', 'distributor', 'reviewer'].map(u => (
              <button
                key={u}
                onClick={() => handleQuickSwitch(DEMO_ACCOUNTS.find(a => a.username === u))}
                className={`rounded-lg py-1 text-center text-[10px] font-bold transition ${
                  username === u ? 'bg-[#1e5d3a] text-white' : 'bg-white text-[#526a5b] hover:bg-[#eaf4ec]'
                }`}
              >
                {u}
              </button>
            ))}
          </div>
        </div>

        <div className="mt-4 flex items-center justify-between border-t border-[#edf2ec] pt-3">
          <span className="text-[11px] font-semibold text-[#829587]">PostgreSQL Active</span>
          <button
            title="Sign out"
            onClick={logout}
            className="flex items-center gap-1.5 rounded-xl border border-[#e1e9e0] px-3 py-1.5 text-xs font-bold text-[#627668] hover:bg-[#f1f5f0]"
          >
            <LogOut size={14} /> Sign out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="lg:pl-[260px]">
        {/* Top Header Bar */}
        <header className="sticky top-0 z-30 flex h-[76px] items-center justify-between border-b border-[#e5ebe4] bg-[#f9fbf8]/95 px-5 backdrop-blur-xl sm:px-8">
          <div className="flex items-center gap-3">
            <button className="rounded-xl border border-[#e2eae1] bg-white p-2 lg:hidden text-[#5c7062]" onClick={() => setMobileNav(true)}>
              <Menu size={18} />
            </button>
            <div>
              <p className="text-[10px] font-extrabold uppercase tracking-[.18em] text-[#86998c]">
                EVERGREEN / {activeNav.toUpperCase()}
              </p>
              <div className="flex items-center gap-2">
                <h2 className="text-sm font-extrabold text-[#193a2a] sm:text-base">
                  {me?.organizationName}
                </h2>
                <Badge color={me?.role === 'ADMIN' ? 'purple' : me?.role === 'REVIEWER' ? 'blue' : 'green'}>
                  {me?.role}
                </Badge>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2.5 sm:gap-3">
            {/* Batch Selector */}
            <div className="hidden items-center gap-2 rounded-xl border border-[#e0eae0] bg-white px-3 py-2 sm:flex">
              <Package size={15} className="text-[#3a8b57]" />
              <span className="text-xs font-semibold text-[#667a6d]">Batch:</span>
              <input
                className="!w-[145px] !border-0 !p-0 !text-xs !font-bold text-[#1f4732] focus:!shadow-none"
                value={currentBatchId}
                onChange={e => setCurrentBatchId(e.target.value)}
              />
              <button onClick={() => refresh()} title="Reload batch data" className="rounded-md bg-[#eaf5ed] p-1 text-[#2d7348]">
                <RefreshCw size={13} />
              </button>
            </div>

            <button
              onClick={() => refresh()}
              title="Refresh dataset"
              className="rounded-xl border border-[#e1e9e0] bg-white p-2.5 text-[#5f7466] hover:bg-[#f3f7f2]"
            >
              <RefreshCw size={16} />
            </button>

            {/* Record Activity CTA (Disabled for Reviewer) */}
            {me?.role !== 'REVIEWER' ? (
              <button
                onClick={() => { setShowForm(true); setNotice('') }}
                className="flex items-center gap-2 rounded-xl bg-[#1a5736] px-4 py-2.5 text-xs font-bold text-white shadow-md shadow-green-950/15 hover:bg-[#134529]"
              >
                <Plus size={16} />
                <span className="hidden sm:inline">Record Activity</span>
                <span className="sm:hidden">Add</span>
              </button>
            ) : (
              <div className="flex items-center gap-1.5 rounded-xl border border-[#dce5dc] bg-[#eff3ef] px-3 py-2 text-xs font-bold text-[#718476]">
                <Lock size={14} /> Read-Only Auditor Mode
              </div>
            )}
          </div>
        </header>

        {/* Global Notices & Alerts */}
        <main className="mx-auto max-w-[1550px] space-y-6 p-5 sm:p-8">
          {error && (
            <div className="flex items-center justify-between rounded-2xl border border-red-200 bg-red-50 p-4 text-xs font-semibold text-red-700 shadow-xs">
              <div className="flex items-center gap-2.5">
                <CircleAlert size={17} className="text-red-600" />
                <span>{error}</span>
              </div>
              <button onClick={() => setError('')}><X size={15} /></button>
            </div>
          )}

          {notice && (
            <div className="flex items-center justify-between rounded-2xl border border-[#c6e6cd] bg-[#eefaf0] p-4 text-xs font-bold text-[#236b3f] shadow-xs">
              <div className="flex items-center gap-2.5">
                <CircleCheck size={17} className="text-[#2c834e]" />
                <span>{notice}</span>
              </div>
              <button onClick={() => setNotice('')}><X size={15} /></button>
            </div>
          )}

          {/* ========================================================================= */}
          {/* VIEW: OVERVIEW */}
          {/* ========================================================================= */}
          {activeNav === 'Overview' && (
            <div className="space-y-6">
              {/* Product Header Banner */}
              <div className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
                <div>
                  <div className="flex items-center gap-2">
                    <Badge color="green">RECONCILED BATCH LEDGER</Badge>
                    <span className="text-[11px] text-[#839789]">Reporting Period: 2026-Q1 · Boundary: Operational Control</span>
                  </div>
                  <h1 className="mt-2 font-display text-3xl font-extrabold tracking-tight text-[#153826] sm:text-4xl">
                    {summary?.productName || 'EcoSmart Circular Thermal Bottle (750ml)'}
                  </h1>
                  <p className="mt-1 text-sm text-[#738679]">
                    Product Code: <span className="font-bold text-[#214a34]">{summary?.productCode || 'PROD-ECO-01'}</span> · Batch Size: <span className="font-bold text-[#214a34]">{fmt(summary?.batchQuantity || 1000, 0)} {summary?.functionalUnit || 'units'}</span>
                  </p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                  <div className="rounded-xl border border-[#e0eae0] bg-white p-1 text-xs font-bold text-[#4e6456]">
                    <span className="px-2.5 py-1 text-[11px] uppercase tracking-wider text-[#8b9e91]">Scope 2 Accounting:</span>
                    <button
                      onClick={() => setScope2Mode('LOCATION')}
                      className={`rounded-lg px-2.5 py-1 transition ${scope2Mode === 'LOCATION' ? 'bg-[#1d5938] text-white' : 'text-[#566c5d] hover:bg-[#f1f6f1]'}`}
                    >
                      Location-Based ({fmt(scope2LocKg, 1)} kg)
                    </button>
                    <button
                      onClick={() => setScope2Mode('MARKET')}
                      className={`rounded-lg px-2.5 py-1 transition ${scope2Mode === 'MARKET' ? 'bg-[#1d5938] text-white' : 'text-[#566c5d] hover:bg-[#f1f6f1]'}`}
                    >
                      Market-Based ({fmt(scope2MktKg, 1)} kg)
                    </button>
                  </div>
                </div>
              </div>

              {/* Primary Carbon Totals Hero Card */}
              <section className="relative overflow-hidden rounded-[28px] bg-gradient-to-br from-[#15442c] via-[#1a4f35] to-[#103522] p-6 text-white shadow-soft sm:p-8">
                <div className="absolute -right-12 -top-24 h-80 w-80 rounded-full border border-white/10" />
                <div className="absolute right-24 top-6 h-44 w-44 rounded-full border border-white/[.07]" />
                <div className="absolute -bottom-24 right-[25%] h-56 w-56 rounded-full bg-[#367f53]/30 blur-3xl" />

                <div className="relative grid gap-8 lg:grid-cols-[1.25fr_.75fr] lg:items-center">
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="flex items-center gap-1.5 text-[11px] font-extrabold uppercase tracking-[.22em] text-[#a5e6b7]">
                        <Sprout size={16} /> RECONCILED PRODUCT FOOTPRINT
                      </p>
                      <Badge color="green">Zero Duplicate Counting</Badge>
                    </div>

                    <div className="mt-4 flex flex-wrap items-baseline gap-x-3 gap-y-1">
                      <span className="font-display text-5xl font-extrabold tracking-tight text-white sm:text-6xl">
                        {fmt(totalReconciledKg, 2)}
                      </span>
                      <span className="text-base font-semibold text-[#b8dfc4]">kg CO₂e total</span>
                      <span className="text-sm text-[#8dbf9c]">({fmt(totalReconciledTonnes, 4)} tonnes CO₂e)</span>
                    </div>

                    <p className="mt-3 max-w-xl text-sm leading-6 text-[#c4decb]">
                      Unique physical emissions across all 9 supply chain stages allocated to batch <span className="font-bold text-white">{currentBatchId}</span>. Supplier Scope 1/2 emissions are tracked separately from buyer Scope 3 inventories to prevent double counting.
                    </p>

                    <div className="mt-6 flex flex-wrap gap-3">
                      {me?.role !== 'REVIEWER' && (
                        <button
                          onClick={() => setShowForm(true)}
                          className="flex items-center gap-2 rounded-xl bg-[#b7f0c4] px-4 py-3 text-xs font-extrabold text-[#15462a] transition hover:bg-[#cbf5d4]"
                        >
                          <Plus size={15} /> Record stage activity
                        </button>
                      )}
                      <button
                        onClick={() => setActiveNav('Product journey')}
                        className="flex items-center gap-2 rounded-xl border border-white/20 bg-white/[.08] px-4 py-3 text-xs font-bold text-white backdrop-blur-sm transition hover:bg-white/[.15]"
                      >
                        <Truck size={15} /> View supply chain timeline
                      </button>
                    </div>
                  </div>

                  {/* Summary Breakdown Pills */}
                  <div className="grid grid-cols-2 gap-3 sm:gap-4">
                    <div className="rounded-2xl border border-white/10 bg-white/[.07] p-4 backdrop-blur-md">
                      <div className="flex items-center justify-between text-xs text-[#bdd8c4]">
                        <span>Cradle-to-Retail</span>
                        <Factory size={16} className="text-[#a5e6b7]" />
                      </div>
                      <p className="mt-3 font-display text-2xl font-extrabold text-white">{fmt(cradleToRetailKg, 1)}</p>
                      <p className="text-[10px] text-[#9bbca4]">kg CO₂e (Direct + Upstream)</p>
                    </div>

                    <div className="rounded-2xl border border-white/10 bg-white/[.07] p-4 backdrop-blur-md">
                      <div className="flex items-center justify-between text-xs text-[#bdd8c4]">
                        <span>Use & End-of-Life</span>
                        <Leaf size={16} className="text-[#a5e6b7]" />
                      </div>
                      <p className="mt-3 font-display text-2xl font-extrabold text-white">{fmt(useAndEolKg, 1)}</p>
                      <p className="text-[10px] text-[#9bbca4]">kg CO₂e (Cat 11 & Cat 12)</p>
                    </div>

                    <div className="rounded-2xl border border-white/10 bg-white/[.07] p-4 backdrop-blur-md">
                      <div className="flex items-center justify-between text-xs text-[#bdd8c4]">
                        <span>Carbon Intensity</span>
                        <Award size={16} className="text-[#a5e6b7]" />
                      </div>
                      <p className="mt-3 font-display text-2xl font-extrabold text-white">{fmt(intensityPerUnit, 3)}</p>
                      <p className="text-[10px] text-[#9bbca4]">kg CO₂e / product unit</p>
                    </div>

                    <div className="rounded-2xl border border-white/10 bg-white/[.07] p-4 backdrop-blur-md">
                      <div className="flex items-center justify-between text-xs text-[#bdd8c4]">
                        <span>Scope 3 Categories</span>
                        <Layers size={16} className="text-[#a5e6b7]" />
                      </div>
                      <p className="mt-3 font-display text-2xl font-extrabold text-white">15 / 15</p>
                      <p className="text-[10px] text-[#9bbca4]">All categories modeled</p>
                    </div>
                  </div>
                </div>
              </section>

              {/* Four Metric KPI Cards */}
              <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <Metric
                  icon={Leaf}
                  label="TOTAL RECONCILED FOOTPRINT"
                  value={fmt(totalReconciledKg, 1)}
                  unit="kg CO₂e"
                  trend="100% reconciled"
                  tone="green"
                  subtitle={`${fmt(totalReconciledTonnes, 4)} metric tonnes`}
                />
                <Metric
                  icon={Factory}
                  label="SCOPE 1 · DIRECT COMBUSTION & GAS"
                  value={fmt(scope1Kg, 1)}
                  unit="kg CO₂e"
                  tone="amber"
                  subtitle="Natural gas boiler, diesel tractor & R-134a refrigerant"
                />
                <Metric
                  icon={Zap}
                  label={`SCOPE 2 · ${scope2Mode === 'LOCATION' ? 'LOCATION-BASED' : 'MARKET-BASED'}`}
                  value={fmt(scope2Mode === 'LOCATION' ? scope2LocKg : scope2MktKg, 1)}
                  unit="kg CO₂e"
                  tone="blue"
                  subtitle={scope2Mode === 'LOCATION' ? 'Regional grid mix average factors' : 'Contractual PPA (0.00 kg CO2e) alternative'}
                />
                <Metric
                  icon={Truck}
                  label="SCOPE 3 · VALUE CHAIN ACTIVITIES"
                  value={fmt(scope3Kg, 1)}
                  unit="kg CO₂e"
                  tone="violet"
                  subtitle="Feedstock, road & rail freight, packaging, use & recycling"
                />
              </section>

              {/* Stage Progress & Scope Distribution */}
              <section className="grid gap-6 xl:grid-cols-[1.1fr_.9fr]">
                {/* Stage Progress Bar Breakdown */}
                <div className="rounded-[22px] border border-[#e4eae2] bg-white p-5 shadow-sm sm:p-6">
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-display text-base font-extrabold text-[#173a29]">Supply Chain Stage Contributions</h3>
                      <p className="mt-0.5 text-xs text-[#7c8f82]">Ordered product journey with incremental and cumulative emissions</p>
                    </div>
                    <Badge color="green">9 Stages Active</Badge>
                  </div>

                  <div className="mt-5 space-y-4">
                    {(summary?.productJourneyStages || []).map(stage => {
                      const maxStageVal = Math.max(1, ...(summary?.productJourneyStages || []).map(s => Number(s.incrementalKgCo2e || 0)))
                      const barWidth = Math.max(3, (Number(stage.incrementalKgCo2e) / maxStageVal) * 100)
                      return (
                        <div key={stage.stageSequence} className="rounded-xl border border-[#edf2ec] bg-[#fbfdfb] p-3 transition hover:border-[#429a6a]">
                          <div className="flex items-center justify-between text-xs">
                            <span className="font-bold text-[#1e4832]">{stage.stageSequence}. {stage.stageName}</span>
                            <span className="font-extrabold text-[#183d28]">
                              {fmt(stage.incrementalKgCo2e, 2)} <span className="text-[10px] font-semibold text-[#7e9285]">kg CO₂e</span>
                            </span>
                          </div>
                          <div className="mt-1 flex items-center justify-between text-[11px] text-[#718578]">
                            <span>Stakeholder: <strong className="text-[#325240]">{stage.organizationName}</strong></span>
                            <span>Cumulative: <strong>{fmt(stage.cumulativeKgCo2e, 2)} kg</strong> ({stage.percentOfTotal}%)</span>
                          </div>
                          <div className="mt-2 h-2 overflow-hidden rounded-full bg-[#eef3ee]">
                            <div
                              className="h-full rounded-full bg-gradient-to-r from-[#2a7b49] to-[#60b679]"
                              style={{ width: `${barWidth}%` }}
                            />
                          </div>
                        </div>
                      )
                    })}
                  </div>
                </div>

                {/* Scope Distribution Doughnut & Guidance */}
                <div className="rounded-[22px] border border-[#e4eae2] bg-white p-5 shadow-sm sm:p-6">
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-display text-base font-extrabold text-[#173a29]">Scope & Category Allocation</h3>
                      <p className="mt-0.5 text-xs text-[#7c8f82]">Breakdown of verified product emissions</p>
                    </div>
                    <Badge color="blue">GHG Protocol</Badge>
                  </div>

                  <div className="mt-6 flex flex-col items-center gap-6 sm:flex-row">
                    <div
                      className="relative grid h-44 w-44 shrink-0 place-items-center rounded-full shadow-inner"
                      style={{
                        background: `conic-gradient(
                          #e69f33 0 ${totalReconciledKg ? (scope1Kg / totalReconciledKg) * 100 : 0}%,
                          #4475cc 0 ${totalReconciledKg ? ((scope1Kg + scope2LocKg) / totalReconciledKg) * 100 : 0}%,
                          #7e57c2 0 100%
                        )`
                      }}
                    >
                      <div className="grid h-[122px] w-[122px] place-items-center rounded-full bg-white text-center shadow-xs">
                        <div>
                          <p className="text-[10px] font-bold uppercase tracking-wider text-[#8b9d91]">RECONCILED</p>
                          <p className="font-display text-xl font-extrabold text-[#183a29]">{fmt(totalReconciledKg, 1)}</p>
                          <p className="text-[10px] font-semibold text-[#8b9d91]">kg CO₂e</p>
                        </div>
                      </div>
                    </div>

                    <div className="w-full space-y-3.5">
                      {[
                        ['Scope 1 (Direct Combustion & Fugitive)', scope1Kg, '#e69f33', 'Boilers, tractors, R-134a'],
                        ['Scope 2 (Purchased Energy - Location)', scope2LocKg, '#4475cc', 'Assembly & warehouse electricity'],
                        ['Scope 3 (Value Chain)', scope3Kg, '#7e57c2', 'Materials, freight, use phase & EOL']
                      ].map(([label, val, color, note]) => (
                        <div key={label} className="rounded-xl border border-[#edf2ec] p-2.5">
                          <div className="flex items-center justify-between text-xs">
                            <span className="flex items-center gap-2 font-bold text-[#234333]">
                              <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: color }} />
                              {label}
                            </span>
                            <span className="font-extrabold text-[#1a3f2b]">{fmt(val, 2)} kg</span>
                          </div>
                          <div className="mt-1 flex items-center justify-between text-[10px] text-[#788a7e]">
                            <span>{note}</span>
                            <span>{totalReconciledKg ? ((Number(val) / totalReconciledKg) * 100).toFixed(1) : '0'}%</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="mt-5 rounded-xl border border-[#dce6dc] bg-[#f7faf7] p-3.5 text-[11px] leading-5 text-[#5e7365]">
                    <strong className="text-[#234c34]">Scope 2 Accounting Compliance:</strong> The GHG Protocol requires reporting Location-Based and Market-Based results as distinct accounting views. Location-based represents physical grid emissions ({fmt(scope2LocKg, 1)} kg), while market-based reflects contractual instruments like the EcoTech zero-carbon PPA ({fmt(scope2MktKg, 1)} kg). <strong>They must never be summed together.</strong>
                  </div>
                </div>
              </section>
            </div>
          )}

          {/* ========================================================================= */}
          {/* VIEW: PRODUCT JOURNEY (SUPPLY CHAIN TRACEABILITY) */}
          {/* ========================================================================= */}
          {activeNav === 'Product journey' && (
            <div className="space-y-6">
              <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
                <div>
                  <Badge color="green">END-TO-END TRACEABILITY</Badge>
                  <h2 className="mt-1 font-display text-2xl font-extrabold text-[#173a28]">
                    Product Supply Chain Journey
                  </h2>
                  <p className="text-xs text-[#75887b]">
                    Chronological lifecycle sequence from Raw Material extraction to End-of-Life recycling.
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-[#64796c]">Batch:</span>
                  <span className="rounded-lg bg-[#e8f4eb] px-3 py-1 text-xs font-extrabold text-[#1f5936]">{currentBatchId}</span>
                </div>
              </div>

              {/* Chronological Timeline */}
              <div className="relative border-l-2 border-[#cbe4d2] ml-4 pl-6 space-y-8 sm:ml-8 sm:pl-10">
                {(summary?.productJourneyStages || []).map((st, idx) => {
                  const stageActivities = (summary?.activities || []).filter(a => a.stageSequence === st.stageSequence)
                  const isEolOrUse = st.stageSequence >= 8
                  return (
                    <div key={st.stageSequence} className="relative">
                      {/* Timeline Dot */}
                      <div className={`absolute -left-[35px] top-1.5 flex h-7 w-7 items-center justify-center rounded-full border-2 border-white text-xs font-extrabold shadow-sm sm:-left-[51px] ${
                        isEolOrUse ? 'bg-[#7e57c2] text-white' : 'bg-[#277848] text-white'
                      }`}>
                        {st.stageSequence}
                      </div>

                      {/* Stage Card */}
                      <div className="rounded-[22px] border border-[#e1e9e0] bg-white p-5 shadow-sm sm:p-6 transition hover:shadow-md">
                        <div className="flex flex-col justify-between gap-2 sm:flex-row sm:items-center">
                          <div>
                            <div className="flex items-center gap-2">
                              <span className="text-xs font-bold uppercase tracking-wider text-[#6b8072]">Stage {st.stageSequence}</span>
                              <Badge color={isEolOrUse ? 'purple' : 'green'}>
                                {isEolOrUse ? 'Use & End-of-Life Phase' : 'Cradle-to-Retail Boundary'}
                              </Badge>
                            </div>
                            <h3 className="mt-1 font-display text-lg font-extrabold text-[#193d2c]">
                              {st.stageName}
                            </h3>
                            <p className="mt-0.5 text-xs text-[#718577]">
                              Responsible Stakeholder: <strong className="text-[#2a593e]">{st.organizationName}</strong>
                            </p>
                          </div>

                          <div className="flex flex-wrap items-baseline gap-3 rounded-xl bg-[#f5f8f5] px-4 py-2.5 sm:text-right">
                            <div>
                              <p className="text-[10px] font-bold uppercase tracking-wider text-[#798e80]">Incremental Stage</p>
                              <p className="font-display text-xl font-extrabold text-[#1a4b30]">
                                {fmt(st.incrementalKgCo2e, 2)} <span className="text-xs font-semibold text-[#718577]">kg CO₂e</span>
                              </p>
                            </div>
                            <div className="border-l border-[#d8e3d7] pl-3">
                              <p className="text-[10px] font-bold uppercase tracking-wider text-[#798e80]">Cumulative Total</p>
                              <p className="font-display text-xl font-extrabold text-[#234b35]">
                                {fmt(st.cumulativeKgCo2e, 2)} <span className="text-xs font-semibold text-[#718577]">kg</span>
                              </p>
                            </div>
                          </div>
                        </div>

                        {/* Activities nested in this stage */}
                        <div className="mt-4 border-t border-[#edf2ec] pt-4">
                          <p className="mb-2 text-[11px] font-extrabold uppercase tracking-wider text-[#829688]">Measured Activity Events</p>
                          <div className="grid gap-2.5 sm:grid-cols-2">
                            {stageActivities.map(act => (
                              <div key={act.id} className="rounded-xl border border-[#e9efe8] bg-[#fafcfa] p-3">
                                <div className="flex items-start justify-between">
                                  <div>
                                    <p className="text-xs font-bold text-[#1f4330]">{act.activityName}</p>
                                    <p className="mt-0.5 text-[11px] text-[#708476]">
                                      {act.inputQuantity} {act.inputUnit} · Factor: {act.factorName}
                                    </p>
                                  </div>
                                  <Badge color={act.scope === 'SCOPE_1' ? 'amber' : act.scope === 'SCOPE_2' ? 'blue' : 'green'}>
                                    {act.scope.replace('_', ' ')}
                                  </Badge>
                                </div>
                                <div className="mt-2 flex items-center justify-between text-[11px] text-[#556d5e]">
                                  <span>Allocation: <strong>{act.allocationPercent}%</strong></span>
                                  <span className="font-extrabold text-[#1d4830]">{fmt(act.allocatedEmissionsKg, 3)} kg CO₂e</span>
                                </div>
                                {act.eventKey && (
                                  <p className="mt-1 text-[9px] font-mono text-[#8b9e91]">Key: {act.eventKey}</p>
                                )}
                              </div>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>

              {/* Linked Logistics Transport Legs Section */}
              <div className="mt-10 rounded-[22px] border border-[#e2e9e1] bg-white p-5 shadow-sm sm:p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="font-display text-base font-extrabold text-[#173a28]">Logistics Transport Legs & Shared Freight</h3>
                    <p className="mt-0.5 text-xs text-[#76897c]">Detailed carrier modes, tonne-kilometres, and shared cargo mass allocations</p>
                  </div>
                  <Badge color="blue">Tonne-Kilometre Engine</Badge>
                </div>

                <div className="mt-5 grid gap-4 md:grid-cols-2">
                  {transfers.map(leg => (
                    <div key={leg.id} className="rounded-2xl border border-[#e6eee5] bg-[#f9fbf9] p-4">
                      <div className="flex items-start justify-between">
                        <div>
                          <span className="font-mono text-[10px] font-bold text-[#2d764a]">{leg.legIdentifier}</span>
                          <h4 className="text-xs font-extrabold text-[#1c402d]">{leg.stageName}</h4>
                        </div>
                        <Badge color={leg.isSharedLeg ? 'amber' : 'green'}>
                          {leg.isSharedLeg ? 'Shared Truck Freight' : 'Dedicated Rail'}
                        </Badge>
                      </div>

                      <div className="mt-3 grid grid-cols-2 gap-2 text-[11px] text-[#5d7364]">
                        <div>From: <strong className="text-[#204531]">{leg.fromOrganization}</strong></div>
                        <div>To: <strong className="text-[#204531]">{leg.toOrganization}</strong></div>
                        <div>Carrier: <strong className="text-[#204531]">{leg.carrierName}</strong></div>
                        <div>Mode: <strong className="text-[#204531]">{leg.transportMode}</strong></div>
                      </div>

                      <div className="mt-3 flex items-center justify-between rounded-xl bg-white p-2.5 text-xs border border-[#edf3ec]">
                        <span>{leg.distanceKm} km × {leg.shippedMassTonnes} t</span>
                        <span>Total: <strong>{leg.tonneKm} t.km</strong></span>
                        <span className="text-[#287449] font-extrabold">Allocated: {leg.allocatedTonneKm} t.km</span>
                      </div>
                      <p className="mt-2 text-[10px] text-[#7f9184]">{leg.notes}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* ========================================================================= */}
          {/* VIEW: ACTIVITY LEDGER */}
          {/* ========================================================================= */}
          {activeNav === 'Activity ledger' && (
            <div className="space-y-5">
              <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
                <div>
                  <h2 className="font-display text-2xl font-extrabold text-[#173a28]">Emission Activities Ledger</h2>
                  <p className="text-xs text-[#75897c]">
                    Immutable calculation snapshots with factor provenance, unit conversions, and allocation records.
                  </p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                  <div className="relative min-w-[200px]">
                    <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#8fa094]" />
                    <input
                      className="!py-2 !pl-9 !text-xs"
                      placeholder="Search activities..."
                      value={search}
                      onChange={e => setSearch(e.target.value)}
                    />
                  </div>

                  <select
                    className="!w-auto !py-2 !text-xs !font-bold"
                    value={scopeFilter}
                    onChange={e => setScopeFilter(e.target.value)}
                  >
                    <option value="ALL">All Scopes</option>
                    <option value="SCOPE_1">Scope 1 (Direct)</option>
                    <option value="SCOPE_2">Scope 2 (Energy)</option>
                    <option value="SCOPE_3">Scope 3 (Value Chain)</option>
                  </select>

                  {me?.role !== 'REVIEWER' && (
                    <button
                      onClick={() => setShowForm(true)}
                      className="flex items-center gap-1.5 rounded-xl bg-[#1a5736] px-3.5 py-2 text-xs font-bold text-white shadow-xs hover:bg-[#124227]"
                    >
                      <Plus size={15} /> Add Activity
                    </button>
                  )}
                </div>
              </div>

              {/* Table */}
              <div className="overflow-hidden rounded-[22px] border border-[#e4eae2] bg-white shadow-soft">
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[900px] text-left">
                    <thead className="border-b border-[#edf2ec] bg-[#f8faf8] text-[10px] font-extrabold uppercase tracking-wider text-[#819487]">
                      <tr>
                        <th className="px-5 py-4">Activity & Stage</th>
                        <th className="px-4 py-4">Scope / Category</th>
                        <th className="px-4 py-4">Input & Conversion</th>
                        <th className="px-4 py-4">Emission Factor</th>
                        <th className="px-4 py-4">Allocation</th>
                        <th className="px-5 py-4 text-right">Calculated Emissions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#edf2ec] text-xs">
                      {filteredActivities.map(a => (
                        <tr key={a.id} className="transition hover:bg-[#f9fbf9]">
                          <td className="px-5 py-4">
                            <p className="font-bold text-[#1f4531]">{a.activityName}</p>
                            <p className="text-[11px] text-[#718577]">
                              {a.stageName} · <span className="font-semibold text-[#325a42]">{a.organizationName}</span>
                            </p>
                            {a.eventKey && (
                              <span className="mt-1 inline-block rounded bg-[#edf4ed] px-1.5 py-0.5 font-mono text-[9px] text-[#2c7247]">
                                Key: {a.eventKey}
                              </span>
                            )}
                          </td>

                          <td className="px-4 py-4">
                            <Badge color={a.scope === 'SCOPE_1' ? 'amber' : a.scope === 'SCOPE_2' ? 'blue' : 'green'}>
                              {a.scope.replace('_', ' ')}
                            </Badge>
                            <p className="mt-1 text-[10px] font-semibold text-[#819286]">{a.categoryCode}</p>
                            {a.isScope2MarketBased && (
                              <Badge color="purple">Market-Based Alternative</Badge>
                            )}
                          </td>

                          <td className="px-4 py-4">
                            <p className="font-semibold text-[#254534]">
                              {a.inputQuantity} {a.inputUnit}
                            </p>
                            {a.inputUnit !== a.standardUnit && (
                              <p className="text-[10px] text-[#788e80]">
                                Converted: {fmt(a.standardQuantity, 2)} {a.standardUnit}
                              </p>
                            )}
                            {a.calculatedTonneKm && (
                              <p className="text-[10px] text-[#337b4f] font-bold">
                                {fmt(a.calculatedTonneKm, 1)} tonne.km
                              </p>
                            )}
                            {a.gasGwpValue && (
                              <p className="text-[10px] text-[#ad651a] font-bold">
                                GWP: {a.gasGwpValue} ({a.gasSpecificName})
                              </p>
                            )}
                          </td>

                          <td className="px-4 py-4">
                            <p className="font-bold text-[#234533]">{a.factorName}</p>
                            <p className="text-[10px] text-[#7d9083]">
                              Factor Snapshot: <strong>{a.factorSnapshot}</strong> kg CO₂e / {a.standardUnit}
                            </p>
                            <span className="text-[9px] font-mono text-[#8a9b8f]">{a.factorCode}</span>
                          </td>

                          <td className="px-4 py-4">
                            <span className="rounded-md bg-[#eef6f0] px-2 py-1 font-bold text-[#276e43]">
                              {a.allocationPercent}%
                            </span>
                            <p className="mt-1 text-[10px] text-[#849589]">
                              {a.allocationNumerator} / {a.allocationDenominator}
                            </p>
                          </td>

                          <td className="px-5 py-4 text-right">
                            <p className="font-display text-sm font-extrabold text-[#1a4b2f]">
                              {fmt(a.allocatedEmissionsKg, 4)}
                            </p>
                            <p className="text-[10px] text-[#86998c]">kg CO₂e</p>
                            {a.rawEmissionsKg !== a.allocatedEmissionsKg && (
                              <p className="text-[9px] text-[#93a498]">Raw: {fmt(a.rawEmissionsKg, 2)} kg</p>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {filteredActivities.length === 0 && (
                    <div className="py-12 text-center text-xs text-[#7e9083]">
                      No activity records found matching criteria.
                    </div>
                  )}
                </div>

                <div className="flex items-center justify-between border-t border-[#edf2ec] bg-[#fafcf9] px-5 py-3 text-[11px] text-[#7c8e82]">
                  <span>Showing {filteredActivities.length} visible records</span>
                  <span>Enforcing backend organization data isolation</span>
                </div>
              </div>
            </div>
          )}

          {/* ========================================================================= */}
          {/* VIEW: SCOPE 3 CATEGORY MATRIX (ALL 15 CATEGORIES + SCOPE 1 & 2) */}
          {/* ========================================================================= */}
          {activeNav === 'Scope 3 matrix' && (
            <div className="space-y-6">
              <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
                <div>
                  <Badge color="green">GHG PROTOCOL COMPLIANCE</Badge>
                  <h2 className="mt-1 font-display text-2xl font-extrabold text-[#173a28]">
                    Scope & Category Status Matrix
                  </h2>
                  <p className="text-xs text-[#75887b]">
                    Transparent coverage status for Scope 1, Scope 2, and all 15 Scope 3 value chain categories. Missing factors are never silently treated as zero.
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Badge color="green">Applicable</Badge>
                  <Badge color="gray">Not Applicable</Badge>
                  <Badge color="amber">Data Unavailable</Badge>
                </div>
              </div>

              <div className="grid gap-3.5 md:grid-cols-2 xl:grid-cols-3">
                {categoryStatusList.map(cat => {
                  const statusColor = cat.status === 'APPLICABLE' ? 'green' : cat.status === 'NOT_APPLICABLE' ? 'gray' : 'amber'
                  return (
                    <div key={cat.code} className="flex flex-col justify-between rounded-2xl border border-[#e2eae1] bg-white p-4 shadow-xs transition hover:shadow-md">
                      <div>
                        <div className="flex items-start justify-between">
                          <span className="font-mono text-[10px] font-extrabold text-[#3a8b57]">{cat.code}</span>
                          <Badge color={statusColor}>{cat.status.replace('_', ' ')}</Badge>
                        </div>
                        <h4 className="mt-2 text-sm font-extrabold text-[#193d2c]">
                          {cat.categoryNumber ? `Cat ${cat.categoryNumber}. ` : ''}{cat.name}
                        </h4>
                        <p className="mt-1 text-xs text-[#6e8275]">{cat.description}</p>
                      </div>

                      <div className="mt-4 border-t border-[#edf2ec] pt-3">
                        <p className="text-[10px] font-extrabold uppercase tracking-wider text-[#839789]">Reason / Boundary Note</p>
                        <p className="mt-1 text-[11px] leading-5 text-[#4e6757]">{cat.reason}</p>
                        <div className="mt-2.5 flex items-center justify-between text-[10px] text-[#7f9386]">
                          <span>Method: <strong>{cat.standardMethod}</strong></span>
                          <span>Boundary: <strong>{cat.lifecycleStage}</strong></span>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {/* ========================================================================= */}
          {/* VIEW: CLIMATIQ FACTOR PROVENANCE CATALOGUE */}
          {/* ========================================================================= */}
          {activeNav === 'Climatiq factors' && (
            <div className="space-y-6">
              <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
                <div>
                  <Badge color="green">CLIMATIQ DATA VERSION 37 CITED</Badge>
                  <h2 className="mt-1 font-display text-2xl font-extrabold text-[#173a28]">
                    Climatiq Emission Factor Catalogue & Provenance
                  </h2>
                  <p className="text-xs text-[#75887b]">
                    Curated licensed emission factors covering all lifecycle stages with full publication provenance, geography, and GWP basis.
                  </p>
                </div>
              </div>

              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                {factors.map(fac => (
                  <div key={fac.id} className="flex flex-col justify-between rounded-2xl border border-[#e2eae1] bg-white p-5 shadow-xs transition hover:shadow-md">
                    <div>
                      <div className="flex items-start justify-between">
                        <span className="font-mono text-[10px] font-extrabold text-[#2a7346]">{fac.factorCode}</span>
                        <Badge color={fac.scope === 'SCOPE_1' ? 'amber' : fac.scope === 'SCOPE_2' ? 'blue' : 'green'}>
                          {fac.scope.replace('_', ' ')}
                        </Badge>
                      </div>

                      <h4 className="mt-2 text-sm font-extrabold text-[#193d2c]">{fac.name}</h4>
                      
                      <div className="mt-3 flex items-baseline gap-2 rounded-xl bg-[#f5f8f5] p-3 border border-[#edf3ec]">
                        <span className="font-display text-2xl font-extrabold text-[#1a4e32]">{fac.factorValue}</span>
                        <span className="text-xs font-semibold text-[#6a8072]">kg CO₂e / {fac.activityUnit}</span>
                      </div>
                    </div>

                    <div className="mt-4 space-y-2 border-t border-[#edf2ec] pt-3 text-[11px] text-[#5c7365]">
                      <div className="flex justify-between"><span>Publisher:</span> <strong className="text-[#204531]">{fac.sourcePublisher}</strong></div>
                      <div className="flex justify-between"><span>Dataset:</span> <strong>{fac.datasetVersion}</strong></div>
                      <div className="flex justify-between"><span>Geography & Year:</span> <strong>{fac.geography} · {fac.factorYear}</strong></div>
                      <div className="flex justify-between"><span>Gas / GWP Basis:</span> <strong>{fac.gasBasis} ({fac.gwpBasis})</strong></div>
                      <div className="flex justify-between"><span>Boundary:</span> <strong>{fac.lifecycleBoundary}</strong></div>
                      <p className="mt-2 text-[10px] text-[#788e80] italic leading-4">{fac.sourceNote}</p>
                    </div>

                    {fac.sourceUrl && (
                      <a
                        href={fac.sourceUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="mt-4 flex items-center justify-center gap-1.5 rounded-xl border border-[#dce6dc] bg-[#f8faf8] py-2 text-xs font-bold text-[#2e6d47] transition hover:bg-[#eef6f0]"
                      >
                        <ExternalLink size={13} /> View Climatiq Source
                      </a>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* ========================================================================= */}
          {/* VIEW: REPORTS & DOUBLE-COUNTING RECONCILIATION */}
          {/* ========================================================================= */}
          {activeNav === 'Double counting report' && (
            <div className="space-y-6">
              <div>
                <Badge color="green">AUDIT & RECONCILIATION</Badge>
                <h2 className="mt-1 font-display text-2xl font-extrabold text-[#173a28]">
                  Double-Counting Prevention & Corporate Inventory Reconciliation
                </h2>
                <p className="text-xs text-[#75887b]">
                  Proving why summing corporate greenhouse gas inventories duplicates emissions across the value chain, and how the unique batch event ledger reconciles the product footprint.
                </p>
              </div>

              {/* Comparison Hero Card */}
              <div className="grid gap-4 sm:grid-cols-3">
                <div className="rounded-2xl border border-[#f5d9bb] bg-[#fffaf5] p-5 shadow-xs">
                  <p className="text-[11px] font-extrabold uppercase tracking-wider text-[#9b631e]">Unadjusted Corporate Inventories</p>
                  <p className="mt-2 font-display text-3xl font-extrabold text-[#874b09]">
                    {fmt(inventoryReport?.sumOfAllCorporateInventoriesKgCo2e, 1)} <span className="text-xs text-[#a06d31]">kg</span>
                  </p>
                  <p className="mt-1 text-[11px] text-[#8e6027]">Sum of every stakeholder's raw inventory (contains duplicate upstream overlaps)</p>
                </div>

                <div className="rounded-2xl border border-[#c4e6cd] bg-[#f3faf5] p-5 shadow-xs">
                  <p className="text-[11px] font-extrabold uppercase tracking-wider text-[#216f40]">Reconciled Product Ledger</p>
                  <p className="mt-2 font-display text-3xl font-extrabold text-[#164c2a]">
                    {fmt(inventoryReport?.reconciledBatchProductFootprintKgCo2e, 1)} <span className="text-xs text-[#327a4d]">kg</span>
                  </p>
                  <p className="mt-1 text-[11px] text-[#3c7853]">Unique physical events allocated once to batch {currentBatchId}</p>
                </div>

                <div className="rounded-2xl border border-[#d2defa] bg-[#f5f8ff] p-5 shadow-xs">
                  <p className="text-[11px] font-extrabold uppercase tracking-wider text-[#355ea8]">Avoided Double-Counting</p>
                  <p className="mt-2 font-display text-3xl font-extrabold text-[#294f94]">
                    {fmt(inventoryReport?.avoidedDoubleCountingDeltaKgCo2e, 1)} <span className="text-xs text-[#446bb0]">kg</span>
                  </p>
                  <p className="mt-1 text-[11px] text-[#4164a2]">Double-counted boundary overlap eliminated by event key ledger</p>
                </div>
              </div>

              {/* Corporate Inventory Table */}
              <div className="overflow-hidden rounded-[22px] border border-[#e4eae2] bg-white shadow-soft">
                <div className="p-5 border-b border-[#edf2ec]">
                  <h3 className="font-display text-base font-extrabold text-[#193d2c]">Corporate Greenhouse Gas Inventories by Stakeholder</h3>
                  <p className="text-xs text-[#728679]">Corporate Scope 1 & 2 emissions vs downstream Scope 3 purchased emissions</p>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full min-w-[700px] text-left">
                    <thead className="bg-[#f8faf8] text-[10px] font-extrabold uppercase tracking-wider text-[#819487]">
                      <tr>
                        <th className="px-5 py-3.5">Organization</th>
                        <th className="px-4 py-3.5">Role</th>
                        <th className="px-4 py-3.5">Scope 1 (Direct)</th>
                        <th className="px-4 py-3.5">Scope 2 (Location)</th>
                        <th className="px-4 py-3.5">Scope 3 (Purchased)</th>
                        <th className="px-5 py-3.5 text-right">Corporate Inventory Total</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#edf2ec] text-xs">
                      {(inventoryReport?.organizationInventories || []).map(row => (
                        <tr key={row.organizationName} className="hover:bg-[#fafcfa]">
                          <td className="px-5 py-3.5 font-bold text-[#1f4531]">{row.organizationName}</td>
                          <td className="px-4 py-3.5"><Badge color="blue">{row.stakeholderRole}</Badge></td>
                          <td className="px-4 py-3.5 font-semibold text-[#875518]">{fmt(row.scope1KgCo2e, 2)} kg</td>
                          <td className="px-4 py-3.5 font-semibold text-[#2b569e]">{fmt(row.scope2LocationKgCo2e, 2)} kg</td>
                          <td className="px-4 py-3.5 font-semibold text-[#5a38a3]">{fmt(row.scope3PurchasedKgCo2e, 2)} kg</td>
                          <td className="px-5 py-3.5 text-right font-extrabold text-[#1c4b2f]">{fmt(row.totalCorporateInventoryKgCo2e, 2)} kg</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="bg-[#f7faf7] p-4 text-xs leading-5 text-[#526a5c] border-t border-[#edf2ec]">
                  <strong className="text-[#1f4a31]">GHG Protocol Standard Explanation:</strong> {inventoryReport?.explanation}
                </div>
              </div>
            </div>
          )}

          {/* ========================================================================= */}
          {/* VIEW: AUDIT TRAIL */}
          {/* ========================================================================= */}
          {activeNav === 'Audit trail' && (
            <div className="space-y-6">
              <div>
                <Badge color="green">IMMUTABLE LOG</Badge>
                <h2 className="mt-1 font-display text-2xl font-extrabold text-[#173a28]">Security & Calculation Audit Trail</h2>
                <p className="text-xs text-[#75887b]">
                  Full audit record of activities created, calculation updates, and RBAC security access checks.
                </p>
              </div>

              <div className="overflow-hidden rounded-[22px] border border-[#e4eae2] bg-white shadow-soft">
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[700px] text-left">
                    <thead className="bg-[#f8faf8] text-[10px] font-extrabold uppercase tracking-wider text-[#819487]">
                      <tr>
                        <th className="px-5 py-3.5">Timestamp</th>
                        <th className="px-4 py-3.5">User</th>
                        <th className="px-4 py-3.5">Organization</th>
                        <th className="px-4 py-3.5">Action</th>
                        <th className="px-5 py-3.5">Details</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#edf2ec] text-xs">
                      {auditLogs.map(log => (
                        <tr key={log.id} className="hover:bg-[#fafcfa]">
                          <td className="px-5 py-3.5 font-mono text-[11px] text-[#718578]">
                            {new Date(log.createdAt).toLocaleString()}
                          </td>
                          <td className="px-4 py-3.5 font-bold text-[#1e4731]">{log.username}</td>
                          <td className="px-4 py-3.5 text-[#516b5a]">{log.organizationName}</td>
                          <td className="px-4 py-3.5">
                            <Badge color={log.action === 'SECURITY_DENIAL' ? 'red' : log.action === 'CREATE' ? 'green' : 'blue'}>
                              {log.action}
                            </Badge>
                          </td>
                          <td className="px-5 py-3.5 font-mono text-[11px] text-[#4d6657] max-w-md truncate">
                            {log.details}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {auditLogs.length === 0 && (
                    <div className="py-12 text-center text-xs text-[#7c9083]">No audit logs recorded yet.</div>
                  )}
                </div>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* ========================================================================= */}
      {/* MODAL: RECORD AN ACTIVITY */}
      {/* ========================================================================= */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0b2416]/50 p-4 backdrop-blur-sm">
          <form
            onSubmit={createActivity}
            className="max-h-[92vh] w-full max-w-3xl overflow-y-auto rounded-[28px] bg-white p-6 shadow-2xl sm:p-8"
          >
            <div className="flex items-start justify-between">
              <div>
                <Badge color="green">CLIMATIQ CALCULATION ENGINE</Badge>
                <h3 className="mt-1 font-display text-2xl font-extrabold text-[#163b29]">Record an Emission Activity</h3>
                <p className="text-xs text-[#73887a]">
                  Backend decimal calculations, unit conversions, and allocation percentage validation applied automatically.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowForm(false)}
                className="rounded-xl bg-[#f2f6f1] p-2 text-[#5e7365] hover:bg-[#e6efe5]"
              >
                <X size={18} />
              </button>
            </div>

            <div className="mt-6 grid gap-4 sm:grid-cols-2">
              {/* Organization */}
              <label className="block text-xs font-bold text-[#455c4e]">
                Organization
                {me?.role === 'ADMIN' ? (
                  <select
                    className="mt-1.5"
                    value={form.organizationName}
                    onChange={e => setForm({ ...form, organizationName: e.target.value })}
                  >
                    {organizations.map(o => (
                      <option key={o.id} value={o.name}>{o.name} ({o.stakeholderRole})</option>
                    ))}
                  </select>
                ) : (
                  <input
                    className="mt-1.5 bg-[#f6f9f6] text-[#2c4e3a] font-semibold cursor-not-allowed"
                    readOnly
                    value={form.organizationName}
                  />
                )}
              </label>

              {/* Batch ID */}
              <label className="block text-xs font-bold text-[#455c4e]">
                Batch Identifier
                <input
                  className="mt-1.5"
                  required
                  value={form.batchId}
                  onChange={e => setForm({ ...form, batchId: e.target.value })}
                />
              </label>

              {/* Stage Sequence & Name */}
              <label className="block text-xs font-bold text-[#455c4e]">
                Supply Chain Stage
                <select
                  className="mt-1.5"
                  value={form.stageName}
                  onChange={e => {
                    const st = e.target.value
                    const seq = Number(st.split('.')[0]) || 1
                    setForm({ ...form, stageName: st, stageSequence: seq })
                  }}
                >
                  <option value="1. Raw Material Sourcing">1. Raw Material Sourcing</option>
                  <option value="2. Upstream Logistics">2. Upstream Logistics</option>
                  <option value="3. Manufacturing & Assembly">3. Manufacturing & Assembly</option>
                  <option value="4. Finished Goods Rail Freight">4. Finished Goods Rail Freight</option>
                  <option value="5. Distribution Hub">5. Distribution Hub</option>
                  <option value="6. Wholesale Staging">6. Wholesale Staging</option>
                  <option value="7. Retail Shelving">7. Retail Shelving</option>
                  <option value="8. Consumer Product Use">8. Consumer Product Use</option>
                  <option value="9. End-of-Life Reclamation">9. End-of-Life Reclamation</option>
                </select>
              </label>

              {/* Activity Name */}
              <label className="block text-xs font-bold text-[#455c4e]">
                Activity Description
                <input
                  className="mt-1.5"
                  required
                  value={form.activityName}
                  onChange={e => setForm({ ...form, activityName: e.target.value })}
                  placeholder="e.g. Boiler Natural Gas Heating"
                />
              </label>

              {/* Scope */}
              <label className="block text-xs font-bold text-[#455c4e]">
                Greenhouse Gas Scope
                <select
                  className="mt-1.5"
                  value={form.scope}
                  onChange={e => {
                    const sc = e.target.value
                    const defaultCat = sc === 'SCOPE_1' ? 'S1_STATIONARY' : sc === 'SCOPE_2' ? 'S2_LOCATION' : 'CAT_1'
                    setForm({ ...form, scope: sc, categoryCode: defaultCat })
                  }}
                >
                  <option value="SCOPE_1">Scope 1 · Direct Emissions</option>
                  <option value="SCOPE_2">Scope 2 · Purchased Energy</option>
                  <option value="SCOPE_3">Scope 3 · Value Chain</option>
                </select>
              </label>

              {/* Category */}
              <label className="block text-xs font-bold text-[#455c4e]">
                Category
                <select
                  className="mt-1.5"
                  value={form.categoryCode}
                  onChange={e => setForm({ ...form, categoryCode: e.target.value })}
                >
                  {categoryStatusList.filter(c => c.scope === form.scope).map(c => (
                    <option key={c.code} value={c.code}>
                      {c.categoryNumber ? `${c.categoryNumber}. ` : ''}{c.name} ({c.status})
                    </option>
                  ))}
                </select>
              </label>

              {/* Emission Factor Selector (Full span) */}
              <label className="block text-xs font-bold text-[#455c4e] sm:col-span-2">
                Climatiq Emission Factor
                <select
                  className="mt-1.5"
                  required
                  value={form.factorId}
                  onChange={e => {
                    const id = e.target.value
                    const fac = factors.find(f => String(f.id) === String(id))
                    setForm(prev => ({
                      ...prev,
                      factorId: id,
                      unit: fac?.activityUnit || prev.unit,
                      isScope2MarketBased: Boolean(fac?.isMarketBasedAlternative)
                    }))
                  }}
                >
                  <option value="">Select an emission factor...</option>
                  {factors
                    .filter(f => f.scope === form.scope)
                    .map(f => (
                      <option key={f.id} value={f.id}>
                        {f.name} · {f.factorValue} kg CO₂e/{f.activityUnit} ({f.sourcePublisher})
                      </option>
                    ))}
                </select>
              </label>

              {/* Quantity & Input Unit */}
              <label className="block text-xs font-bold text-[#455c4e]">
                Activity Input Quantity
                <input
                  className="mt-1.5"
                  type="number"
                  min="0.00000001"
                  step="any"
                  required
                  value={form.quantity}
                  onChange={e => setForm({ ...form, quantity: e.target.value })}
                />
              </label>

              <label className="block text-xs font-bold text-[#455c4e]">
                Input Unit (Automatic conversion applied)
                <select
                  className="mt-1.5"
                  value={form.unit}
                  onChange={e => setForm({ ...form, unit: e.target.value })}
                >
                  {['kg', 'tonne', 'lb', 'g', 'kWh', 'MWh', 'MJ', 'GJ', 'litre', 'gal', 'm3', 'km', 'mile', 'tonne.km', 'USD'].map(u => (
                    <option key={u} value={u}>{u}</option>
                  ))}
                </select>
              </label>

              {/* Freight Specific Inputs (if applicable) */}
              {selectedFactor?.activityUnit.includes('tonne.km') && (
                <>
                  <label className="block text-xs font-bold text-[#455c4e]">
                    Shipped Cargo Mass (Tonnes)
                    <input
                      className="mt-1.5"
                      type="number"
                      step="any"
                      placeholder="e.g. 2.5"
                      value={form.shippedMassTonnes}
                      onChange={e => setForm({ ...form, shippedMassTonnes: e.target.value })}
                    />
                  </label>
                  <label className="block text-xs font-bold text-[#455c4e]">
                    Distance Traveled (Kilometres)
                    <input
                      className="mt-1.5"
                      type="number"
                      step="any"
                      placeholder="e.g. 450"
                      value={form.distanceKm}
                      onChange={e => setForm({ ...form, distanceKm: e.target.value })}
                    />
                  </label>
                </>
              )}

              {/* Refrigerant Gas GWP (if Scope 1 Fugitive) */}
              {form.categoryCode === 'S1_FUGITIVE' && (
                <label className="block text-xs font-bold text-[#455c4e] sm:col-span-2">
                  Refrigerant Gas (IPCC AR5 100-yr GWP Applied)
                  <select
                    className="mt-1.5"
                    value={form.gasSpecificName}
                    onChange={e => setForm({ ...form, gasSpecificName: e.target.value })}
                  >
                    <option value="R-134a">R-134a (GWP: 1,300)</option>
                    <option value="R-410A">R-410A (GWP: 1,924)</option>
                    <option value="CH4">CH4 - Methane (GWP: 28)</option>
                    <option value="N2O">N2O - Nitrous Oxide (GWP: 265)</option>
                    <option value="SF6">SF6 - Sulfur Hexafluoride (GWP: 23,500)</option>
                  </select>
                </label>
              )}

              {/* Allocation Numerator & Denominator */}
              <label className="block text-xs font-bold text-[#455c4e]">
                Allocation Numerator (Certified Basis)
                <input
                  className="mt-1.5"
                  type="number"
                  min="0.00000001"
                  step="any"
                  required
                  value={form.allocationNumerator}
                  onChange={e => setForm({ ...form, allocationNumerator: e.target.value })}
                />
              </label>

              <label className="block text-xs font-bold text-[#455c4e]">
                Allocation Denominator (Total Facility/Cargo)
                <input
                  className="mt-1.5"
                  type="number"
                  min="0.00000001"
                  step="any"
                  required
                  value={form.allocationDenominator}
                  onChange={e => setForm({ ...form, allocationDenominator: e.target.value })}
                />
              </label>

              {/* Physical Event Key */}
              <label className="block text-xs font-bold text-[#455c4e] sm:col-span-2">
                Physical Emission Event Key (Optional Unique Id)
                <input
                  className="mt-1.5 font-mono text-xs"
                  placeholder="e.g. EVENT-BATCH-2026-LEG-01"
                  value={form.eventKey}
                  onChange={e => setForm({ ...form, eventKey: e.target.value })}
                />
                <span className="mt-1 block text-[10px] text-[#7d9284]">
                  Reuse identical event keys to represent shared physical events; duplicate keys for independent events are rejected to prevent double counting.
                </span>
              </label>

              {/* Method & Assumptions Notes */}
              <label className="block text-xs font-bold text-[#455c4e] sm:col-span-2">
                Method & Assumptions Notes
                <textarea
                  className="mt-1.5"
                  rows={2}
                  value={form.methodNote}
                  onChange={e => setForm({ ...form, methodNote: e.target.value })}
                  placeholder="Reporting year, factor boundaries, meter certificate..."
                />
              </label>
            </div>

            {/* Live Calculation Preview Banner */}
            {calculatedPreview && (
              <div className="mt-6 rounded-2xl border border-[#cbe4d2] bg-[#f2f8f3] p-4 text-xs">
                <div className="flex items-center justify-between">
                  <span className="font-extrabold text-[#235836]">Live Calculation Preview</span>
                  <Badge color="green">{calculatedPreview.allocPct}% Allocation</Badge>
                </div>
                <div className="mt-2 flex items-baseline gap-2">
                  <span className="font-display text-2xl font-extrabold text-[#194b2e]">
                    {fmt(calculatedPreview.allocKg, 4)} kg CO₂e
                  </span>
                  <span className="text-[11px] text-[#6e8375]">
                    (Raw: {fmt(calculatedPreview.rawKg, 2)} kg)
                  </span>
                </div>
                <p className="mt-1 text-[10px] text-[#718678]">
                  Formula: Quantity × Unit Conversion × Factor Snapshot × (Numerator / Denominator)
                </p>
              </div>
            )}

            <div className="mt-6 flex flex-col-reverse gap-3 border-t border-[#edf2ec] pt-5 sm:flex-row sm:justify-end">
              <button
                type="button"
                onClick={() => setShowForm(false)}
                className="rounded-xl border border-[#dce5db] px-5 py-3 text-xs font-bold text-[#5c7263]"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="flex items-center justify-center gap-2 rounded-xl bg-[#1b5536] px-6 py-3 text-xs font-bold text-white shadow-md shadow-green-950/15 hover:bg-[#124227]"
              >
                <Check size={16} /> Save and Calculate
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
