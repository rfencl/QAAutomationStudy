// const { auth } = NextAuth(authConfig);
import { auth } from "@/auth";
import { DEFAULT_LOGIN_REDIRECT, apiAuthPrefix, uiAuthPrefix, publicRoutes, apiSecuredRoutes } from "@/routes";
import { NextResponse } from "next/server";

export default auth((req): void | Response | Promise<void | Response> => {
  const { nextUrl } = req;
  const isLoggedIn = !!req.auth;

  const isApiAuthRoute = nextUrl.pathname.startsWith(apiAuthPrefix);
  const isUiAuthRoute = nextUrl.pathname.startsWith(uiAuthPrefix);
  const isPublicRoute = publicRoutes.includes(nextUrl.pathname);
  const isApiSecuredRoute = apiSecuredRoutes.some((p) => nextUrl.pathname.includes(p));

  if (isApiAuthRoute) return;

  if (isApiSecuredRoute && !isPublicRoute) {
    if (!isLoggedIn) return NextResponse.json({ error: "Your token has expired or you aren't logged in!" }, { status: 401 });
    return;
  }

  if (isUiAuthRoute) {
    if (isLoggedIn) return Response.redirect(new URL(DEFAULT_LOGIN_REDIRECT, nextUrl));
    return;
  }

  if (!isLoggedIn && !isPublicRoute) {
    // let callbackUrl = nextUrl.pathname;
    // if (nextUrl.search) {
    //   callbackUrl += nextUrl.search;
    // }

    // const encodedCallbackUrl = encodeURIComponent(callbackUrl);

    // return Response.redirect(new URL(`/auth/login?callbackUrl=${encodedCallbackUrl}`, nextUrl));
    return Response.redirect(new URL("/auth/login", nextUrl));
  }

  return;
});

// Optionally, don't invoke Middleware on some paths
export const config = {
  matcher: ["/((?!.+\\.[\\w]+$|_next).*)", "/", "/(api|trpc)(.*)"],
};
