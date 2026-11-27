import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bot, Info, MessageSquarePlus, Send, User } from "lucide-react";
import { assistantApi, farmApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatDateTime } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Field";
import { Breadcrumbs } from "@/components/ui/Feedback";

const QUICK_PROMPTS = [
  "What irrigation schedule fits my active wheat or tomato crop?",
  "How should I interpret my latest soil pH and NPK levels before top dressing?",
  "What preventive pest scouting steps should I follow this week?",
  "When should I avoid foliar spraying based on weather conditions?",
];

export function AssistantPage() {
  const queryClient = useQueryClient();
  const { error } = useToast();
  const [selectedConvId, setSelectedConvId] = useState<string | null>(null);
  const [selectedFarmId, setSelectedFarmId] = useState<string>("");
  const [question, setQuestion] = useState("");

  const { data: status } = useQuery({ queryKey: ["assistant", "status"], queryFn: assistantApi.status });
  const { data: farms } = useQuery({ queryKey: ["farms", "all"], queryFn: () => farmApi.list(undefined, 0, 50) });
  const { data: conversations } = useQuery({
    queryKey: ["assistant", "conversations"],
    queryFn: assistantApi.conversations,
  });
  const { data: messages } = useQuery({
    queryKey: ["assistant", "messages", selectedConvId],
    queryFn: () => assistantApi.messages(selectedConvId!),
    enabled: Boolean(selectedConvId),
  });

  const askMutation = useMutation({
    mutationFn: (text: string) =>
      assistantApi.ask({
        conversationId: selectedConvId,
        farmId: selectedFarmId || null,
        question: text,
      }),
    onSuccess: (res) => {
      setSelectedConvId(res.conversationId);
      setQuestion("");
      queryClient.invalidateQueries({ queryKey: ["assistant", "conversations"] });
      queryClient.invalidateQueries({ queryKey: ["assistant", "messages", res.conversationId] });
    },
    onError: (err) => error("Assistant request failed", extractErrorMessage(err)),
  });

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!question.trim()) return;
    askMutation.mutate(question.trim());
  };

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[{ label: "Dashboard", to: "/app/farmer" }, { label: "AI Agricultural Assistant" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Agricultural Assistant</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Ask cultivation, irrigation, fertilizer, and pest management questions grounded in your authorized farm records.
          </p>
        </div>
        <Badge tone={status?.aiProviderConfigured ? "green" : "amber"}>
          {status?.aiProviderConfigured ? `AI Provider: ${status.configuredProvider}` : "Non-AI Reference Mode"}
        </Badge>
      </div>

      {status && !status.aiProviderConfigured && (
        <div className="flex items-start gap-2.5 rounded-xl border border-amber-200 bg-amber-50/75 p-3.5 text-xs text-amber-950 dark:border-amber-900/60 dark:bg-amber-950/30 dark:text-amber-200">
          <Info className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{status.notice}</span>
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-12">
        {/* Conversations Sidebar */}
        <Card className="space-y-4 lg:col-span-4">
          <div className="flex items-center justify-between">
            <h2 className="font-bold">Conversations</h2>
            <Button
              size="sm"
              variant="secondary"
              onClick={() => setSelectedConvId(null)}
              icon={<MessageSquarePlus className="h-4 w-4" />}
            >
              New Chat
            </Button>
          </div>

          <Select
            label="Authorize Farm Context"
            value={selectedFarmId}
            onChange={(e) => setSelectedFarmId(e.target.value)}
          >
            <option value="">General Agronomy (No Farm Context)</option>
            {(farms?.items || []).map((f) => (
              <option key={f.id} value={f.id}>
                {f.name} ({f.soilType})
              </option>
            ))}
          </Select>

          <div className="space-y-2 pt-2">
            {(conversations || []).map((c) => (
              <button
                key={c.id}
                type="button"
                onClick={() => setSelectedConvId(c.id)}
                className={`w-full rounded-xl border p-3 text-left text-xs transition ${
                  selectedConvId === c.id
                    ? "border-[#10402E] bg-[#EEF3EE] dark:border-[#4FD085] dark:bg-[#173026]"
                    : "border-[#DCE5DC] hover:bg-[#F7FAF7] dark:border-[#203D31] dark:hover:bg-[#132920]"
                }`}
              >
                <p className="truncate font-semibold">{c.title}</p>
                <p className="mt-1 text-[11px] text-[#4B6358] dark:text-[#9EB7AB]">
                  {c.providerName} • {formatDateTime(c.updatedAt)}
                </p>
              </button>
            ))}
            {(!conversations || conversations.length === 0) && (
              <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">No saved conversations yet.</p>
            )}
          </div>
        </Card>

        {/* Chat Area */}
        <Card className="flex min-h-[500px] flex-col justify-between lg:col-span-8">
          <div className="flex-1 space-y-4 overflow-y-auto pr-1">
            {!selectedConvId || !messages || messages.length === 0 ? (
              <div className="space-y-4 py-6">
                <div className="flex items-center gap-2 text-[#10402E] dark:text-[#4FD085]">
                  <Bot className="h-6 w-6" />
                  <h3 className="text-lg font-bold">Start an Agronomic Conversation</h3>
                </div>
                <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
                  Select one of your farms on the left to share soil, crop, and weather context, or click a quick question below:
                </p>
                <div className="grid gap-2.5 sm:grid-cols-2">
                  {QUICK_PROMPTS.map((prompt) => (
                    <button
                      key={prompt}
                      type="button"
                      onClick={() => askMutation.mutate(prompt)}
                      className="rounded-xl border border-[#DCE5DC] bg-[#FBFBF8] p-3 text-left text-xs font-medium hover:border-[#2A8C55] dark:border-[#244337] dark:bg-[#0D1E17]"
                    >
                      {prompt}
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              messages.map((m) => (
                <div
                  key={m.id}
                  className={`flex gap-3 ${m.senderRole === "USER" ? "justify-end" : "justify-start"}`}
                >
                  <div
                    className={`max-w-[85%] rounded-2xl p-4 text-sm ${
                      m.senderRole === "USER"
                        ? "bg-[#10402E] text-white dark:bg-[#2A8C55]"
                        : "border border-[#DCE5DC] bg-[#F7FAF7] text-[#11211B] dark:border-[#244337] dark:bg-[#132920] dark:text-[#EEF4F0]"
                    }`}
                  >
                    <div className="mb-1 flex items-center gap-2 text-[11px] opacity-80">
                      {m.senderRole === "USER" ? <User className="h-3.5 w-3.5" /> : <Bot className="h-3.5 w-3.5" />}
                      <span className="font-semibold">
                        {m.senderRole === "USER"
                          ? "You"
                          : m.aiGenerated
                            ? "AI Assistant"
                            : "Agronomic Reference Guide"}
                      </span>
                    </div>
                    <p className="whitespace-pre-line leading-relaxed">{m.content}</p>
                    {m.senderRole === "ASSISTANT" && m.contextSummary && (
                      <p className="mt-2 border-t border-black/10 pt-2 text-[11px] opacity-75 dark:border-white/10">
                        Context: {m.contextSummary}
                      </p>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>

          <form onSubmit={handleSend} className="mt-4 flex gap-2.5 border-t border-[#DCE5DC] pt-4 dark:border-[#203D31]">
            <input
              type="text"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder="Ask about crop stages, irrigation, fertilizers, or pest observations..."
              className="min-h-11 flex-1 rounded-xl border border-[#C9D6CA] bg-white px-4 text-sm focus:border-[#2A8C55] focus:outline-none dark:border-[#244337] dark:bg-[#0D1E17]"
            />
            <Button type="submit" loading={askMutation.isPending} icon={<Send className="h-4 w-4" />}>
              Send
            </Button>
          </form>
        </Card>
      </div>
    </div>
  );
}
