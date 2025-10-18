// /auth/login

"use client";

import * as z from "zod";

import { useEffect, useState, useTransition } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { LoginSchema } from "@/schemas";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { CardWrapper } from "@/components/auth/card-wrapper";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { FormSuccess } from "@/components/form-success";
import { FormError } from "@/components/form-error";
import { login } from "@/actions/actionsAuth";
import { DEFAULT_LOGIN_REDIRECT } from "@/routes";

export default function LoginPage() {
  const [isPending, startTransition] = useTransition();
  const [error, setError] = useState<string | undefined>("");
  const [success, setSuccess] = useState<string | undefined>("");
  const form = useForm<z.infer<typeof LoginSchema>>({
    resolver: zodResolver(LoginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = (values: z.infer<typeof LoginSchema>) => {
    setError("");
    setSuccess("");
    startTransition(() => {
      login(values, DEFAULT_LOGIN_REDIRECT).then((data) => {
        setError(data?.error);
        setSuccess(data?.success);
      });
    });
  };

  useEffect(() => {
    document.title = "Census App | Login";
  }, []);

  return (
    <main className="flex min-h-full flex-1 flex-col justify-center px-6 py-12 lg:px-8">
      <CardWrapper headerLabel="Welcome back!" backButtonLabel="Don't have an account?" backButtonHref="/auth/register">
        <Form {...form}>
          <form className="login-form space-y-6" onSubmit={form.handleSubmit(onSubmit)}>
            <div className="space-y-4">
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel id="email-label">Email</FormLabel>
                    <FormControl>
                      <Input {...field} id="email" placeholder="john.doe@example.com" type="email" disabled={isPending} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel id="password-label">Password</FormLabel>
                    <FormControl>
                      <Input {...field} id="password" type="password" disabled={isPending} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <FormError message={error} />
            <FormSuccess message={success} />
            <Button type="submit" id="login-button" className="login-button btn w-full" disabled={isPending}>
              Login
            </Button>
          </form>
        </Form>
      </CardWrapper>
    </main>
  );
}
