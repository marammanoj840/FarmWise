import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { CheckCircle2, Mail, MapPin, Phone } from "lucide-react";
import { publicApi } from "@/api/services";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage } from "@/utils/format";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";

const contactSchema = z.object({
  name: z.string().min(2, "Enter your full name"),
  email: z.string().email("Enter a valid email address"),
  phone: z.string().optional(),
  roleInterest: z.string().optional(),
  subject: z.string().min(3, "Subject must be at least 3 characters"),
  message: z.string().min(10, "Message must be at least 10 characters"),
});

type ContactFormValues = z.infer<typeof contactSchema>;

export function ContactPage() {
  const { success, error } = useToast();
  const [submitted, setSubmitted] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ContactFormValues>({
    resolver: zodResolver(contactSchema),
    defaultValues: { roleInterest: "FARMER" },
  });

  const onSubmit = async (values: ContactFormValues) => {
    try {
      await publicApi.submitContact(values);
      setSubmitted(true);
      success("Inquiry submitted", "Thank you! Our team has recorded your message.");
      reset();
    } catch (err) {
      error("Submission failed", extractErrorMessage(err));
    }
  };

  return (
    <div className="mx-auto max-w-6xl px-4 py-14 sm:px-6 lg:px-8">
      <div className="grid gap-10 lg:grid-cols-12">
        <div className="space-y-6 lg:col-span-5">
          <Badge tone="green">Get in Touch</Badge>
          <h1 className="font-display text-4xl font-bold">Contact FarmWise</h1>
          <p className="text-sm leading-relaxed text-[#4B6358] dark:text-[#9EB7AB]">
            Questions about onboarding your farm, FPO, agronomy practice, input catalog, or bulk sourcing? Send us a
            note — inquiries are stored directly in our backend administrative inbox.
          </p>

          <div className="space-y-4">
            <Card padding="p-4" className="flex items-start gap-3">
              <Mail className="mt-0.5 h-5 w-5 text-[#2A8C55]" />
              <div>
                <p className="text-sm font-bold">Support & Partnerships</p>
                <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">support@farmwise.local</p>
              </div>
            </Card>
            <Card padding="p-4" className="flex items-start gap-3">
              <Phone className="mt-0.5 h-5 w-5 text-[#2A8C55]" />
              <div>
                <p className="text-sm font-bold">Farmer Helpdesk Hours</p>
                <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">Mon–Sat, 09:00 – 18:00 IST</p>
              </div>
            </Card>
            <Card padding="p-4" className="flex items-start gap-3">
              <MapPin className="mt-0.5 h-5 w-5 text-[#2A8C55]" />
              <div>
                <p className="text-sm font-bold">Primary Coverage</p>
                <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">India (INR ₹ default, localization-ready)</p>
              </div>
            </Card>
          </div>
        </div>

        <div className="lg:col-span-7">
          <Card>
            {submitted ? (
              <div className="space-y-4 py-6 text-center">
                <CheckCircle2 className="mx-auto h-12 w-12 text-[#2A8C55]" />
                <h2 className="text-2xl font-bold">Inquiry Logged Successfully</h2>
                <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
                  Your message has been saved for FarmWise administration review.
                </p>
                <Button variant="outline" onClick={() => setSubmitted(false)}>
                  Send Another Message
                </Button>
              </div>
            ) : (
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div className="grid gap-4 sm:grid-cols-2">
                  <Input label="Full Name" required {...register("name")} error={errors.name?.message} />
                  <Input label="Email Address" type="email" required {...register("email")} error={errors.email?.message} />
                </div>
                <div className="grid gap-4 sm:grid-cols-2">
                  <Input label="Phone (Optional)" placeholder="+91-..." {...register("phone")} />
                  <Select label="I am a" {...register("roleInterest")}>
                    <option value="FARMER">Farmer / FPO</option>
                    <option value="EXPERT">Agricultural Expert</option>
                    <option value="SUPPLIER">Input Supplier</option>
                    <option value="BUYER">Produce Buyer</option>
                    <option value="PARTNER">Agribusiness / Enterprise</option>
                  </Select>
                </div>
                <Input label="Subject" required {...register("subject")} error={errors.subject?.message} />
                <Textarea label="Message" rows={4} required {...register("message")} error={errors.message?.message} />
                <Button type="submit" loading={isSubmitting} className="w-full" size="lg">
                  Submit Inquiry
                </Button>
              </form>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
