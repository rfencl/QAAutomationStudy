// /auth/register
// "use server";

// import { RegisterForm } from "@/components/auth/register-form";

// const RegisterPage = () => {
//   return <RegisterForm />;
// };

// export default RegisterPage;

"use client";

import * as z from "zod";
import { useState, useTransition } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { RegisterUserSchema } from "@/schemas";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { FormError } from "@/components/form-error";
import { FormSuccess } from "@/components/form-success";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { CardWrapper } from "@/components/auth/card-wrapper";
import { useRouter } from "next/navigation";

import { AvatarDialog } from "@/components/auth/avatar-dialog";
import { avatarLinks } from "@/lib/avatars";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { register } from "@/actions/actionsUser";

export default function RegisterPage() {
  const router = useRouter();
  const [error, setError] = useState<string | undefined>("");
  const [success, setSuccess] = useState<string | undefined>("");
  const [isPending, startTransition] = useTransition();
  const form = useForm<z.infer<typeof RegisterUserSchema>>({
    resolver: zodResolver(RegisterUserSchema),
    defaultValues: {
      email: "",
      password: "",
      name: "",
      image: "",
    },
  });
  const getRandomProfileImage = () => {
    const r: number = Math.floor(Math.random() * avatarLinks.length);
    return avatarLinks[r];
  };
  const [avatar, setAvatar] = useState<string>(getRandomProfileImage());
  const [isDialogOpen, setDialogOpen] = useState(false);

  const onSubmit = (values: z.infer<typeof RegisterUserSchema>) => {
    setError("");
    setSuccess("");
    startTransition(() => {
      values.image = avatar;
      register(values).then((data) => {
        setError(data.error);
        setSuccess(data.success);
      });
    });
  };

  return (
    <CardWrapper headerLabel="Sign up" backButtonLabel="Already have an account?" backButtonHref="/auth/login">
      <Form {...form}>
        <div className="w-full flex justify-center content-center">
          <Avatar
            onClick={() => {
              setDialogOpen(true);
            }}
          >
            <AvatarImage src={avatar} alt="@shadcn" />
            <AvatarFallback>CN</AvatarFallback>
          </Avatar>
          <AvatarDialog isDialogOpen={isDialogOpen} setDialogOpen={setDialogOpen} setAvatar={setAvatar} />
        </div>
        <form name="register-form" onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
          <div className="space-y-4">
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input id="name" {...field} disabled={isPending} placeholder="John Doe"></Input>
                  </FormControl>
                  <FormMessage></FormMessage>
                </FormItem>
              )}
            ></FormField>
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Email</FormLabel>
                  <FormControl>
                    <Input id="email" {...field} disabled={isPending} placeholder="john.doe@example.com" type="email"></Input>
                  </FormControl>
                  <FormMessage></FormMessage>
                </FormItem>
              )}
            ></FormField>
            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Password</FormLabel>
                  <FormControl>
                    <Input id="password" {...field} disabled={isPending} placeholder="******" type="password"></Input>
                  </FormControl>
                  <FormMessage></FormMessage>
                </FormItem>
              )}
            ></FormField>
          </div>
          <FormError message={error} />
          <FormSuccess message={success} />
          {success ? (
            <Button
              name="login-button btn"
              type="button"
              className="w-full"
              disabled={isPending}
              onClick={() => {
                router.push("/auth/login");
              }}
            >
              Login
            </Button>
          ) : (
            <Button name="sign-up-button" type="submit" className="w-full" disabled={isPending}>
              Sign up
            </Button>
          )}
        </form>
      </Form>
    </CardWrapper>
  );
}
