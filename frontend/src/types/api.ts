export type UserRole = "FARMER" | "EXPERT" | "SUPPLIER" | "BUYER" | "ADMIN";
export type SupportedLocale = "en" | "hi" | "pa" | "mr" | "ta";

export interface PagedResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface MessageResponse {
  message: string;
  timestamp: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
  fieldErrors?: { field: string; message: string }[];
}

export interface UserSummary {
  id: string;
  email: string;
  fullName: string;
  phone?: string;
  primaryRole: UserRole;
  roles: string[];
  active: boolean;
  emailVerified: boolean;
  lastLoginAt?: string;
}

export interface AuthResponse {
  tokenType: string;
  accessToken: string;
  expiresInSeconds: number;
  refreshToken: string;
  user: UserSummary;
  devEmailVerificationToken?: string | null;
}

export interface FullUserProfile extends UserSummary {
  userId: string;
  avatarUrl?: string;
  bio?: string;
  addressLine?: string;
  villageOrCity?: string;
  district?: string;
  state?: string;
  country: string;
  postalCode?: string;
  preferredLanguage: string;
  preferredCurrency: string;
  farmExperienceYears?: number;
}

export interface SessionItem {
  id: string;
  userAgent?: string;
  ipAddress?: string;
  createdAt: string;
  expiresAt: string;
}

export interface FarmLocation {
  addressLine?: string;
  villageOrCity: string;
  district: string;
  state: string;
  country?: string;
  postalCode?: string;
  latitude?: number | null;
  longitude?: number | null;
  climateZone?: string;
}

export interface Farm {
  id: string;
  farmerId: string;
  name: string;
  landArea: number;
  areaUnit: string;
  soilType: string;
  irrigationType: string;
  photoUrls: string[];
  description?: string;
  active: boolean;
  location: FarmLocation;
  createdAt: string;
  updatedAt: string;
}

export interface CropVariety {
  id: string;
  cropId: string;
  name: string;
  maturityDays?: number;
  expectedYieldPerAcreKg?: number;
  climateNotes?: string;
  diseaseResistance?: string;
}

export interface CropCatalogItem {
  id: string;
  name: string;
  scientificName?: string;
  category: string;
  typicalDurationDays?: number;
  waterRequirement?: string;
  optimalPhMin?: number;
  optimalPhMax?: number;
  seasonSuitability?: string;
  description?: string;
  varieties: CropVariety[];
}

export interface FarmCrop {
  id: string;
  farmId: string;
  farmName: string;
  farmerId: string;
  cropId?: string;
  varietyId?: string;
  cropName: string;
  varietyName?: string;
  season: string;
  plantedArea: number;
  areaUnit: string;
  sowingDate?: string;
  expectedHarvestDate?: string;
  actualHarvestDate?: string;
  growthStage: string;
  cultivationStatus: string;
  expectedYieldKg?: number;
  actualYieldKg?: number;
  fieldObservations?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CultivationActivity {
  id: string;
  farmId: string;
  farmCropId?: string;
  stage: string;
  title: string;
  activityDate: string;
  inputUsed?: string;
  quantity?: number;
  unit?: string;
  costAmount: number;
  notes?: string;
  createdAt: string;
}

export interface CultivationTask {
  id: string;
  farmId: string;
  farmCropId?: string;
  stage: string;
  title: string;
  dueDate: string;
  reminderAt?: string;
  priority: string;
  status: string;
  notes?: string;
  completedAt?: string;
  createdAt: string;
}

export interface InventoryItem {
  id: string;
  farmId?: string;
  name: string;
  category: string;
  sku?: string;
  quantity: number;
  unit: string;
  reorderThreshold: number;
  lowStock: boolean;
  unitCostInr: number;
  supplierName?: string;
  notes?: string;
  updatedAt: string;
}

export interface InventoryTransaction {
  id: string;
  inventoryItemId: string;
  transactionType: string;
  quantityDelta: number;
  unitCostInr: number;
  referenceNote?: string;
  transactionDate: string;
  createdAt: string;
}

export interface Expense {
  id: string;
  farmId?: string;
  farmName?: string;
  farmCropId?: string;
  cropName?: string;
  category: string;
  title: string;
  amountInr: number;
  currency: string;
  expenseDate: string;
  vendorName?: string;
  paymentMethod?: string;
  notes?: string;
  createdAt: string;
}

export interface Income {
  id: string;
  farmId?: string;
  farmName?: string;
  farmCropId?: string;
  cropName?: string;
  category: string;
  title: string;
  amountInr: number;
  currency: string;
  incomeDate: string;
  buyerName?: string;
  quantitySold?: number;
  unit?: string;
  notes?: string;
  createdAt: string;
}

export interface FinanceSummary {
  currency: string;
  totalIncomeInr: number;
  totalExpensesInr: number;
  netProfitLossInr: number;
  expensesByCategory: Record<string, number>;
  incomeByCategory: Record<string, number>;
  byFarm: {
    farmId: string;
    farmName: string;
    totalIncomeInr: number;
    totalExpensesInr: number;
    netBalanceInr: number;
  }[];
  byCrop: {
    farmCropId: string;
    cropName: string;
    totalIncomeInr: number;
    totalExpensesInr: number;
    netBalanceInr: number;
  }[];
  disclaimer: string;
}

export interface DailyForecastItem {
  date: string;
  tempMaxC?: number;
  tempMinC?: number;
  precipitationMm?: number;
  precipitationProbabilityPct?: number;
}

export interface WeatherResponse {
  farmId?: string;
  locationName: string;
  latitude?: number;
  longitude?: number;
  liveDataAvailable: boolean;
  providerName: string;
  statusMessage: string;
  temperatureC?: number;
  humidityPct?: number;
  rainfallMm?: number;
  windSpeedKmh?: number;
  conditionSummary: string;
  alertLevel: string;
  recommendations: string[];
  dailyForecast: DailyForecastItem[];
  observedAt: string;
}

export interface SoilRecord {
  id: string;
  farmId: string;
  farmName: string;
  soilType: string;
  ph: number;
  nitrogen?: number;
  phosphorus?: number;
  potassium?: number;
  organicCarbonPct?: number;
  moisturePct?: number;
  fertilityRating?: string;
  labName?: string;
  testDate: string;
  observations?: string;
  createdAt: string;
}

export interface CropObservation {
  id: string;
  farmId: string;
  farmCropId?: string;
  observationType: string;
  severity: string;
  title: string;
  symptoms: string;
  affectedAreaPct?: number;
  recommendedAction?: string;
  expertVerified: boolean;
  observedAt: string;
  advisoryDisclaimer: string;
}

export interface CropKnowledgeItem {
  id: string;
  cropName: string;
  category: string;
  suitableSoilTypes: string;
  idealPhRange: string;
  sowingSeason: string;
  irrigationSchedule: string;
  nutrientGuidance: string;
  commonPestsDiseases: string;
  advisoryDisclaimer: string;
}

export interface SuitabilityReport {
  soilType: string;
  ph: number;
  professionallyVerified: boolean;
  uncertaintyDisclaimer: string;
  candidates: {
    cropName: string;
    category: string;
    matchConfidenceNote: string;
    seasonSuitability?: string;
    phRange: string;
    waterRequirement?: string;
  }[];
}

export interface AssistantStatus {
  aiProviderConfigured: boolean;
  configuredProvider: string;
  notice: string;
}

export interface AssistantConversation {
  id: string;
  farmId?: string;
  title: string;
  providerName: string;
  updatedAt: string;
}

export interface AssistantMessage {
  id: string;
  senderRole: "USER" | "ASSISTANT";
  content: string;
  aiGenerated: boolean;
  contextSummary?: string;
  createdAt: string;
}

export interface AskAssistantResponse {
  conversationId: string;
  title: string;
  aiProviderConfigured: boolean;
  providerName: string;
  contextSummary: string;
  userMessage: AssistantMessage;
  assistantMessage: AssistantMessage;
  referenceTopics: string[];
}

export interface ProductCategory {
  id: string;
  name: string;
  slug: string;
  description?: string;
  iconName: string;
}

export interface Product {
  id: string;
  supplierId: string;
  supplierName: string;
  supplierVerified: boolean;
  categoryId: string;
  categoryName: string;
  name: string;
  slug: string;
  description: string;
  unit: string;
  priceInr: number;
  stockQuantity: number;
  brand?: string;
  imageUrl?: string;
  approvalStatus: string;
  active: boolean;
  createdAt: string;
}

export interface SupplierProfile {
  id: string;
  userId: string;
  businessName: string;
  gstin?: string;
  description?: string;
  city?: string;
  state?: string;
  phone?: string;
  verificationStatus: string;
}

export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  unit: string;
  unitPriceInr: number;
  quantity: number;
  lineTotalInr: number;
  availableStock: number;
  imageUrl?: string;
}

export interface Cart {
  cartId: string;
  items: CartItem[];
  subtotalInr: number;
  totalItems: number;
  paymentGatewayConfigured: boolean;
  paymentProvider: string;
}

export interface OrderItem {
  id: string;
  orderId: string;
  productId?: string;
  supplierId: string;
  productName: string;
  unit: string;
  unitPriceInr: number;
  quantity: number;
  lineTotalInr: number;
  fulfillmentStatus: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  status: string;
  paymentStatus: string;
  paymentProvider: string;
  paymentNotice: string;
  totalAmountInr: number;
  currency: string;
  shippingName: string;
  shippingPhone: string;
  shippingAddress: string;
  shippingCity: string;
  shippingState: string;
  shippingPostalCode: string;
  notes?: string;
  items: OrderItem[];
  createdAt: string;
}

export interface ProduceListing {
  id: string;
  farmerId: string;
  farmerName: string;
  farmId?: string;
  cropName: string;
  variety?: string;
  quantity: number;
  unit: string;
  expectedPriceInr: number;
  location: string;
  qualityGrade: string;
  qualityNotes?: string;
  availableFrom?: string;
  harvestDate?: string;
  listingStatus: string;
  moderationStatus: string;
  createdAt: string;
}

export interface ProduceInquiry {
  id: string;
  produceListingId: string;
  cropName: string;
  buyerUserId: string;
  buyerName: string;
  offeredPriceInr?: number;
  requestedQuantity: number;
  message: string;
  status: string;
  farmerReply?: string;
  createdAt: string;
}

export interface MarketPriceItem {
  id: string;
  commodity: string;
  variety?: string;
  marketName: string;
  district: string;
  state: string;
  minPriceInr: number;
  maxPriceInr: number;
  modalPriceInr: number;
  unit: string;
  priceDate: string;
  dataSource: string;
  sampleData: boolean;
  fetchedAt: string;
}

export interface MarketPriceFeed {
  liveFeedConfigured: boolean;
  providerName: string;
  containsSampleData: boolean;
  notice: string;
  commodities: string[];
  markets: string[];
  prices: MarketPriceItem[];
}

export interface ExpertProfile {
  id: string;
  userId: string;
  fullName: string;
  headline: string;
  specializations: string;
  qualifications?: string;
  experienceYears: number;
  languages: string;
  city?: string;
  state?: string;
  consultationFeeInr: number;
  bio?: string;
  verificationStatus: string;
  verified: boolean;
  verifiedAt?: string;
  available: boolean;
}

export interface Consultation {
  id: string;
  farmerId: string;
  farmerName: string;
  expertId: string;
  expertName: string;
  expertVerified: boolean;
  farmId?: string;
  topic: string;
  cropName?: string;
  question: string;
  urgency: string;
  status: string;
  expertResponse?: string;
  respondedAt?: string;
  createdAt: string;
}

export interface Appointment {
  id: string;
  consultationRequestId?: string;
  farmerId: string;
  farmerName: string;
  expertId: string;
  expertName: string;
  scheduledStart: string;
  scheduledEnd: string;
  meetingMode: string;
  meetingNotes?: string;
  status: string;
}

export interface AppNotification {
  id: string;
  notificationType: string;
  title: string;
  message: string;
  read: boolean;
  readAt?: string;
  actionUrl?: string;
  createdAt: string;
}

export interface NotificationPreference {
  inAppEnabled: boolean;
  emailEnabled: boolean;
  smsEnabled: boolean;
  cultivationReminders: boolean;
  weatherAlerts: boolean;
  inventoryAlerts: boolean;
  orderUpdates: boolean;
  expertUpdates: boolean;
  emailProviderConfigured: boolean;
  smsProviderConfigured: boolean;
}

export interface FarmerDashboardSummary {
  totalFarms: number;
  activeCrops: number;
  totalEstimatedYieldKg: number;
  pendingTasksCount: number;
  lowStockInventoryCount: number;
  unreadNotificationsCount: number;
  totalExpensesInr: number;
  totalIncomeInr: number;
  netBalanceInr: number;
  financeDisclaimer: string;
  primaryFarmWeather: WeatherResponse;
  recentActivities: CultivationActivity[];
  upcomingTasks: CultivationTask[];
  recentExpenses: Expense[];
  marketPriceHighlights: MarketPriceItem[];
  marketPricesAreSample: boolean;
}

export interface AdminStats {
  totalUsers: number;
  activeUsers: number;
  usersByRole: Record<string, number>;
  totalFarms: number;
  totalActiveCrops: number;
  totalProducts: number;
  pendingProducts: number;
  activeProduceListings: number;
  totalOrders: number;
  totalExperts: number;
  totalSuppliers: number;
  openContactInquiries: number;
}
