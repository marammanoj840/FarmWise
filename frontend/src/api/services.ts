import { apiClient } from "@/api/client";
import type {
  AdminStats,
  AppNotification,
  Appointment,
  AskAssistantResponse,
  AssistantConversation,
  AssistantMessage,
  AssistantStatus,
  AuthResponse,
  Cart,
  Consultation,
  CropCatalogItem,
  CropKnowledgeItem,
  CropObservation,
  CultivationActivity,
  CultivationTask,
  Expense,
  ExpertProfile,
  Farm,
  FarmCrop,
  FarmerDashboardSummary,
  FinanceSummary,
  FullUserProfile,
  Income,
  InventoryItem,
  InventoryTransaction,
  MarketPriceFeed,
  MessageResponse,
  NotificationPreference,
  Order,
  OrderItem,
  PagedResponse,
  ProduceInquiry,
  ProduceListing,
  Product,
  ProductCategory,
  SessionItem,
  SoilRecord,
  SuitabilityReport,
  SupplierProfile,
  UserSummary,
  WeatherResponse,
} from "@/types/api";

export const authApi = {
  register: (payload: Record<string, unknown>) =>
    apiClient.post<AuthResponse>("/auth/register", payload).then((r) => r.data),
  login: (payload: { email: string; password: string }) =>
    apiClient.post<AuthResponse>("/auth/login", payload).then((r) => r.data),
  logout: (refreshToken?: string | null) =>
    apiClient.post<MessageResponse>("/auth/logout", { refreshToken }).then((r) => r.data),
  me: () => apiClient.get<UserSummary>("/auth/me").then((r) => r.data),
  forgotPassword: (email: string) =>
    apiClient
      .post<{ message: string; devResetTokenPreview?: string | null }>("/auth/forgot-password", { email })
      .then((r) => r.data),
  resetPassword: (payload: { token: string; newPassword: string }) =>
    apiClient.post<MessageResponse>("/auth/reset-password", payload).then((r) => r.data),
  verifyEmail: (token: string) =>
    apiClient.post<MessageResponse>("/auth/verify-email", { token }).then((r) => r.data),
  resendVerification: () =>
    apiClient
      .post<{ message: string; devResetTokenPreview?: string | null }>("/auth/resend-verification")
      .then((r) => r.data),
};

export const userApi = {
  getProfile: () => apiClient.get<FullUserProfile>("/users/me/profile").then((r) => r.data),
  updateProfile: (payload: Partial<FullUserProfile>) =>
    apiClient.put<FullUserProfile>("/users/me/profile", payload).then((r) => r.data),
  changePassword: (payload: { currentPassword: string; newPassword: string }) =>
    apiClient.put<MessageResponse>("/users/me/password", payload).then((r) => r.data),
  getSessions: () => apiClient.get<SessionItem[]>("/users/me/sessions").then((r) => r.data),
  revokeSession: (id: string) =>
    apiClient.delete<MessageResponse>(`/users/me/sessions/${id}`).then((r) => r.data),
  revokeAllSessions: () =>
    apiClient.delete<MessageResponse>("/users/me/sessions").then((r) => r.data),
};

export const publicApi = {
  overview: () =>
    apiClient
      .get<{
        supportedCrops: number;
        productCategories: number;
        approvedProducts: number;
        registeredExperts: number;
        defaultCurrency: string;
      }>("/public/overview")
      .then((r) => r.data),
  submitContact: (payload: {
    name: string;
    email: string;
    phone?: string;
    roleInterest?: string;
    subject: string;
    message: string;
  }) => apiClient.post<MessageResponse>("/public/contact", payload).then((r) => r.data),
};

export const farmerApi = {
  dashboard: () => apiClient.get<FarmerDashboardSummary>("/farmer/dashboard").then((r) => r.data),
};

export const farmApi = {
  list: (q?: string, page = 0, size = 20) =>
    apiClient.get<PagedResponse<Farm>>("/farms", { params: { q, page, size } }).then((r) => r.data),
  get: (id: string) => apiClient.get<Farm>(`/farms/${id}`).then((r) => r.data),
  create: (payload: Record<string, unknown>) => apiClient.post<Farm>("/farms", payload).then((r) => r.data),
  update: (id: string, payload: Record<string, unknown>) =>
    apiClient.put<Farm>(`/farms/${id}`, payload).then((r) => r.data),
  remove: (id: string) => apiClient.delete<MessageResponse>(`/farms/${id}`).then((r) => r.data),
};

export const cropApi = {
  catalog: () => apiClient.get<CropCatalogItem[]>("/crops/catalog").then((r) => r.data),
  list: (farmId?: string, page = 0, size = 25) =>
    apiClient.get<PagedResponse<FarmCrop>>("/crops", { params: { farmId, page, size } }).then((r) => r.data),
  create: (payload: Record<string, unknown>) => apiClient.post<FarmCrop>("/crops", payload).then((r) => r.data),
  update: (id: string, payload: Record<string, unknown>) =>
    apiClient.put<FarmCrop>(`/crops/${id}`, payload).then((r) => r.data),
  remove: (id: string) => apiClient.delete<MessageResponse>(`/crops/${id}`).then((r) => r.data),
};

export const cultivationApi = {
  listActivities: (farmId?: string, page = 0, size = 25) =>
    apiClient
      .get<PagedResponse<CultivationActivity>>("/cultivation/activities", { params: { farmId, page, size } })
      .then((r) => r.data),
  createActivity: (payload: Record<string, unknown>) =>
    apiClient.post<CultivationActivity>("/cultivation/activities", payload).then((r) => r.data),
  deleteActivity: (id: string) =>
    apiClient.delete<MessageResponse>(`/cultivation/activities/${id}`).then((r) => r.data),
  listTasks: (status?: string, page = 0, size = 25) =>
    apiClient
      .get<PagedResponse<CultivationTask>>("/cultivation/tasks", { params: { status, page, size } })
      .then((r) => r.data),
  createTask: (payload: Record<string, unknown>) =>
    apiClient.post<CultivationTask>("/cultivation/tasks", payload).then((r) => r.data),
  updateTaskStatus: (id: string, status: string) =>
    apiClient.patch<CultivationTask>(`/cultivation/tasks/${id}/status`, { status }).then((r) => r.data),
  deleteTask: (id: string) =>
    apiClient.delete<MessageResponse>(`/cultivation/tasks/${id}`).then((r) => r.data),
};

export const inventoryApi = {
  list: (category?: string, page = 0, size = 25) =>
    apiClient
      .get<PagedResponse<InventoryItem>>("/inventory", { params: { category, page, size } })
      .then((r) => r.data),
  create: (payload: Record<string, unknown>) =>
    apiClient.post<InventoryItem>("/inventory", payload).then((r) => r.data),
  update: (id: string, payload: Record<string, unknown>) =>
    apiClient.put<InventoryItem>(`/inventory/${id}`, payload).then((r) => r.data),
  remove: (id: string) => apiClient.delete<MessageResponse>(`/inventory/${id}`).then((r) => r.data),
  recordTransaction: (id: string, payload: Record<string, unknown>) =>
    apiClient.post<InventoryItem>(`/inventory/${id}/transactions`, payload).then((r) => r.data),
  history: (id: string) =>
    apiClient.get<InventoryTransaction[]>(`/inventory/${id}/transactions`).then((r) => r.data),
};

export const financeApi = {
  listExpenses: (page = 0, size = 25) =>
    apiClient.get<PagedResponse<Expense>>("/finance/expenses", { params: { page, size } }).then((r) => r.data),
  createExpense: (payload: Record<string, unknown>) =>
    apiClient.post<Expense>("/finance/expenses", payload).then((r) => r.data),
  deleteExpense: (id: string) => apiClient.delete<MessageResponse>(`/finance/expenses/${id}`).then((r) => r.data),
  listIncome: (page = 0, size = 25) =>
    apiClient.get<PagedResponse<Income>>("/finance/income", { params: { page, size } }).then((r) => r.data),
  createIncome: (payload: Record<string, unknown>) =>
    apiClient.post<Income>("/finance/income", payload).then((r) => r.data),
  deleteIncome: (id: string) => apiClient.delete<MessageResponse>(`/finance/income/${id}`).then((r) => r.data),
  summary: () => apiClient.get<FinanceSummary>("/finance/summary").then((r) => r.data),
  exportReport: (type: string, format: "CSV" | "PDF") =>
    apiClient
      .get<Blob>("/finance/reports/export", {
        params: { type, format },
        responseType: "blob",
      })
      .then((r) => r.data),
};

export const weatherApi = {
  forFarm: (farmId: string) => apiClient.get<WeatherResponse>(`/weather/farm/${farmId}`).then((r) => r.data),
  lookup: (location: string, latitude?: number, longitude?: number) =>
    apiClient
      .get<WeatherResponse>("/weather/lookup", { params: { location, latitude, longitude } })
      .then((r) => r.data),
};

export const soilApi = {
  listRecords: (page = 0, size = 25) =>
    apiClient.get<PagedResponse<SoilRecord>>("/soil/records", { params: { page, size } }).then((r) => r.data),
  createRecord: (payload: Record<string, unknown>) =>
    apiClient.post<SoilRecord>("/soil/records", payload).then((r) => r.data),
  deleteRecord: (id: string) => apiClient.delete<MessageResponse>(`/soil/records/${id}`).then((r) => r.data),
  listObservations: (page = 0, size = 25) =>
    apiClient
      .get<PagedResponse<CropObservation>>("/soil/observations", { params: { page, size } })
      .then((r) => r.data),
  createObservation: (payload: Record<string, unknown>) =>
    apiClient.post<CropObservation>("/soil/observations", payload).then((r) => r.data),
  knowledgeBase: (q?: string) =>
    apiClient.get<CropKnowledgeItem[]>("/soil/knowledge", { params: { q } }).then((r) => r.data),
  suitability: (soilType: string, ph: number) =>
    apiClient.get<SuitabilityReport>("/soil/knowledge/suitability", { params: { soilType, ph } }).then((r) => r.data),
};

export const assistantApi = {
  status: () => apiClient.get<AssistantStatus>("/assistant/status").then((r) => r.data),
  conversations: () => apiClient.get<AssistantConversation[]>("/assistant/conversations").then((r) => r.data),
  messages: (id: string) =>
    apiClient.get<AssistantMessage[]>(`/assistant/conversations/${id}/messages`).then((r) => r.data),
  ask: (payload: { conversationId?: string | null; farmId?: string | null; question: string }) =>
    apiClient.post<AskAssistantResponse>("/assistant/ask", payload).then((r) => r.data),
};

export const marketplaceApi = {
  categories: () => apiClient.get<ProductCategory[]>("/marketplace/categories").then((r) => r.data),
  products: (params: { categoryId?: string; q?: string; sort?: string; page?: number; size?: number }) =>
    apiClient.get<PagedResponse<Product>>("/products", { params }).then((r) => r.data),
  createProduct: (payload: Record<string, unknown>) =>
    apiClient.post<Product>("/products", payload).then((r) => r.data),
  updateProduct: (id: string, payload: Record<string, unknown>) =>
    apiClient.put<Product>(`/products/${id}`, payload).then((r) => r.data),
  supplierProfile: () => apiClient.get<SupplierProfile>("/marketplace/supplier/profile").then((r) => r.data),
  updateSupplierProfile: (payload: Record<string, unknown>) =>
    apiClient.put<SupplierProfile>("/marketplace/supplier/profile", payload).then((r) => r.data),
  supplierProducts: () => apiClient.get<Product[]>("/marketplace/supplier/products").then((r) => r.data),
  supplierOrders: () => apiClient.get<OrderItem[]>("/marketplace/supplier/orders").then((r) => r.data),
  updateOrderItemStatus: (itemId: string, fulfillmentStatus: string) =>
    apiClient
      .patch<OrderItem>(`/marketplace/supplier/orders/items/${itemId}/status`, { fulfillmentStatus })
      .then((r) => r.data),
  getCart: () => apiClient.get<Cart>("/marketplace/cart").then((r) => r.data),
  addToCart: (productId: string, quantity = 1) =>
    apiClient.post<Cart>("/marketplace/cart/items", { productId, quantity }).then((r) => r.data),
  updateCartItem: (itemId: string, quantity: number) =>
    apiClient.patch<Cart>(`/marketplace/cart/items/${itemId}`, { quantity }).then((r) => r.data),
  removeCartItem: (itemId: string) =>
    apiClient.delete<Cart>(`/marketplace/cart/items/${itemId}`).then((r) => r.data),
  checkout: (payload: Record<string, unknown>) =>
    apiClient.post<Order>("/orders", payload).then((r) => r.data),
  myOrders: (page = 0, size = 20) =>
    apiClient.get<PagedResponse<Order>>("/orders", { params: { page, size } }).then((r) => r.data),
  listProduce: (q?: string, page = 0, size = 20) =>
    apiClient.get<PagedResponse<ProduceListing>>("/produce", { params: { q, page, size } }).then((r) => r.data),
  myProduce: () => apiClient.get<ProduceListing[]>("/marketplace/farmer/produce").then((r) => r.data),
  createProduce: (payload: Record<string, unknown>) =>
    apiClient.post<ProduceListing>("/produce", payload).then((r) => r.data),
  deleteProduce: (id: string) => apiClient.delete<MessageResponse>(`/produce/${id}`).then((r) => r.data),
  submitProduceInquiry: (id: string, payload: Record<string, unknown>) =>
    apiClient.post<ProduceInquiry>(`/produce/${id}/inquiries`, payload).then((r) => r.data),
  listingInquiries: (id: string) =>
    apiClient.get<ProduceInquiry[]>(`/produce/${id}/inquiries`).then((r) => r.data),
  myInquiries: () => apiClient.get<ProduceInquiry[]>("/marketplace/buyer/inquiries").then((r) => r.data),
  replyInquiry: (inquiryId: string, payload: { status: string; farmerReply: string }) =>
    apiClient.patch<ProduceInquiry>(`/produce/inquiries/${inquiryId}/reply`, payload).then((r) => r.data),
};

export const marketPriceApi = {
  search: (params?: { commodity?: string; state?: string; market?: string }) =>
    apiClient.get<MarketPriceFeed>("/market-prices", { params }).then((r) => r.data),
};

export const expertApi = {
  list: (q?: string, verifiedOnly?: boolean) =>
    apiClient.get<ExpertProfile[]>("/experts", { params: { q, verifiedOnly } }).then((r) => r.data),
  myProfile: () => apiClient.get<ExpertProfile>("/experts/me/profile").then((r) => r.data),
  updateMyProfile: (payload: Record<string, unknown>) =>
    apiClient.put<ExpertProfile>("/experts/me/profile", payload).then((r) => r.data),
  createConsultation: (payload: Record<string, unknown>) =>
    apiClient.post<Consultation>("/consultations", payload).then((r) => r.data),
  myConsultations: () => apiClient.get<Consultation[]>("/consultations/mine").then((r) => r.data),
  assignedConsultations: () =>
    apiClient.get<Consultation[]>("/consultations/expert/assigned").then((r) => r.data),
  respondConsultation: (id: string, payload: { status: string; expertResponse: string }) =>
    apiClient.patch<Consultation>(`/consultations/${id}/respond`, payload).then((r) => r.data),
  scheduleAppointment: (payload: Record<string, unknown>) =>
    apiClient.post<Appointment>("/consultations/appointments", payload).then((r) => r.data),
  appointments: () => apiClient.get<Appointment[]>("/consultations/appointments").then((r) => r.data),
};

export const notificationApi = {
  list: (unreadOnly?: boolean, page = 0, size = 20) =>
    apiClient
      .get<PagedResponse<AppNotification>>("/notifications", { params: { unreadOnly, page, size } })
      .then((r) => r.data),
  unreadCount: () => apiClient.get<{ unreadCount: number }>("/notifications/unread-count").then((r) => r.data),
  markRead: (id: string) => apiClient.patch<AppNotification>(`/notifications/${id}/read`).then((r) => r.data),
  markAllRead: () => apiClient.patch<MessageResponse>("/notifications/read-all").then((r) => r.data),
  preferences: () => apiClient.get<NotificationPreference>("/notifications/preferences").then((r) => r.data),
  updatePreferences: (payload: Partial<NotificationPreference>) =>
    apiClient.put<NotificationPreference>("/notifications/preferences", payload).then((r) => r.data),
};

export const adminApi = {
  stats: () => apiClient.get<AdminStats>("/admin/stats").then((r) => r.data),
  users: (role?: string, page = 0, size = 25) =>
    apiClient.get<PagedResponse<UserSummary>>("/admin/users", { params: { role, page, size } }).then((r) => r.data),
  updateUserStatus: (id: string, active: boolean) =>
    apiClient.patch<UserSummary>(`/admin/users/${id}/status`, { active }).then((r) => r.data),
  updateUserRole: (id: string, role: string) =>
    apiClient.patch<UserSummary>(`/admin/users/${id}/role`, { role }).then((r) => r.data),
  verifyExpert: (expertId: string, status: string, note?: string) =>
    apiClient.patch<MessageResponse>(`/admin/experts/${expertId}/verify`, { status, note }).then((r) => r.data),
  suppliers: () => apiClient.get<SupplierProfile[]>("/admin/suppliers").then((r) => r.data),
  verifySupplier: (supplierId: string, status: string, note?: string) =>
    apiClient.patch<MessageResponse>(`/admin/suppliers/${supplierId}/verify`, { status, note }).then((r) => r.data),
  products: (page = 0, size = 25) =>
    apiClient.get<PagedResponse<Product>>("/admin/products", { params: { page, size } }).then((r) => r.data),
  moderateProduct: (id: string, status: string, note?: string) =>
    apiClient.patch<Product>(`/admin/products/${id}/moderate`, { status, note }).then((r) => r.data),
  produce: (page = 0, size = 25) =>
    apiClient.get<PagedResponse<ProduceListing>>("/admin/produce", { params: { page, size } }).then((r) => r.data),
  moderateProduce: (id: string, status: string, note?: string) =>
    apiClient.patch<ProduceListing>(`/admin/produce/${id}/moderate`, { status, note }).then((r) => r.data),
  auditLogs: (action?: string, page = 0, size = 25) =>
    apiClient
      .get<
        PagedResponse<{
          id: string;
          actorEmail?: string;
          action: string;
          entityType: string;
          entityId?: string;
          detailsJson: string;
          ipAddress?: string;
          createdAt: string;
        }>
      >("/admin/audit-logs", { params: { action, page, size } })
      .then((r) => r.data),
  contactInquiries: (page = 0, size = 25) =>
    apiClient
      .get<
        PagedResponse<{
          id: string;
          name: string;
          email: string;
          subject: string;
          message: string;
          status: string;
          adminNotes?: string;
          createdAt: string;
        }>
      >("/admin/contact-inquiries", { params: { page, size } })
      .then((r) => r.data),
  updateContactInquiry: (id: string, status: string, adminNotes?: string) =>
    apiClient.patch(`/admin/contact-inquiries/${id}`, { status, adminNotes }).then((r) => r.data),
  broadcastNotification: (payload: {
    targetUserId?: string;
    notificationType: string;
    title: string;
    message: string;
    actionUrl?: string;
  }) => apiClient.post<MessageResponse>("/admin/notifications", payload).then((r) => r.data),
  systemHealth: () =>
    apiClient
      .get<{
        coreHealth: { status: string; databaseStatus: string; service: string; version: string };
        integrations: { integration: string; provider: string; configured: boolean; statusNote: string }[];
      }>("/admin/system-health")
      .then((r) => r.data),
};
