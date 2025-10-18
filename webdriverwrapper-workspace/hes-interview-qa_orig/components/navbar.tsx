"use client";
import { AuthUser } from "@/types/types";
import { Disclosure, DisclosureButton, DisclosurePanel, Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/react";
import { Bars3Icon, BellIcon, XMarkIcon } from "@heroicons/react/24/outline";
import { signOut } from "next-auth/react";
import Image from "next/image";

import { usePathname } from "next/navigation";

function classNames(...classes: string[]) {
  return classes.filter(Boolean).join(" ");
}

interface NavigationProps {
  name: string;
  href: string;
  current: boolean;
}

interface ProfileProps {
  src: string;
  menuItems: {
    text: string;
    href: string;
    onclick?: () => {} | void;
  }[];
}

export default function Navbar({ authUser }: { authUser: AuthUser | null }) {
  const pathname = usePathname();
  let navigation: NavigationProps[] = [
    { name: "About", href: "/", current: pathname == "/" },
    { name: "Docs", href: "/documentation", current: pathname == "/documentation" },
    { name: "Swagger", href: "#", current: false },
  ];

  let profile: ProfileProps;

  if (authUser) {
    if (authUser.role == "ADMIN")
      navigation = [
        { name: "Dashboard", href: "/dashboard", current: pathname == "/dashboard" },
        { name: "Records", href: "/records", current: pathname == "/records" },
        ...navigation,
      ];
    else navigation = [{ name: "Dashboard", href: "/dashboard", current: pathname == "/dashboard" }, ...navigation];
    profile = {
      src: authUser.image || "/account.svg",
      menuItems: [
        {
          text: "You profile",
          href: "/profile",
        },
        {
          text: "Settings",
          href: "#",
        },
        {
          text: "Sign out",
          href: "#",
          onclick: () => {
            signOut();
          },
        },
      ],
    };
  } else {
    profile = {
      src: "/account.svg",
      menuItems: [
        {
          text: "Sign in",
          href: "/auth/login",
        },
        {
          text: "Register",
          href: "/auth/register",
        },
      ],
    };
  }

  return (
    <Disclosure as="nav" className="bg-gray-800">
      {({ open }) => (
        <>
          <div className="mx-auto max-w-7xl px-2 sm:px-6 lg:px-8">
            <div className="nav-menu-options relative flex h-16 items-center justify-between" data-testid="navbar">
              <div className="absolute inset-y-0 left-0 flex items-center sm:hidden">
                {/* Mobile menu button*/}
                <DisclosureButton
                  id="nav-menu-button"
                  data-testid="mobile-navbar"
                  className="relative inline-flex items-center justify-center rounded-md p-2 text-gray-400 hover:bg-gray-700 hover:text-white focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
                >
                  <span className="absolute -inset-0.5" />
                  <span className="sr-only">Open main menu</span>
                  {open ? <XMarkIcon className="block h-6 w-6" aria-hidden="true" /> : <Bars3Icon className="block h-6 w-6" aria-hidden="true" />}
                </DisclosureButton>
              </div>
              <div className="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
                <div className="flex flex-shrink-0 items-center">
                  <Image className="h-8 w-8 rounded-full" src="/logo.jpg" alt="Logo" width="8000" height="8000" data-testid="logo" />
                </div>
                <div className="sm:ml-6 sm:block">
                  <div className="nav-bar-buttons flex space-x-4" data-testid="navbar-buttons">
                    {navigation.map((item) => (
                      <a
                        key={item.name}
                        data-testid={`navbar-button-${item.name.replaceAll(/\s+/g, "-")}`}
                        href={item.href}
                        className={classNames(
                          item.current ? "bg-gray-900 text-white" : "text-gray-300 hover:bg-gray-700 hover:text-white",
                          "rounded-md px-3 py-2 text-sm font-medium",
                          "hidden sm:block"
                        )}
                        aria-current={item.current ? "page" : undefined}
                      >
                        {item.name}
                      </a>
                    ))}
                  </div>
                </div>
              </div>
              <div className="absolute inset-y-0 right-0 flex items-center pr-2 sm:static sm:inset-auto sm:ml-6 sm:pr-0">
                <button
                  type="button"
                  className="notification-button relative rounded-full bg-gray-800 p-1 text-gray-400 hover:text-white focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800"
                  data-testid="notification-bell"
                >
                  <span className="absolute -inset-1.5" />
                  <span className="sr-only">View notifications</span>
                  <BellIcon className="h-6 w-6" aria-hidden="true" />
                </button>

                {/* Profile dropdown */}
                <Menu as="div" className="relative ml-3">
                  <div>
                    <MenuButton
                      id="user-menu-button"
                      className="relative flex rounded-full bg-gray-800 text-sm focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800"
                      data-testid="avatar-button"
                    >
                      <span className="absolute -inset-1.5" />
                      <span className="sr-only">Open user menu</span>
                      {/* <Image className="h-8 w-8 rounded-full object-cover" src={profile.src} alt="" width="8" height="8" /> */}
                      <Image className="h-8 w-8 rounded-full" src={profile.src} alt="" width="256" height="256" />
                    </MenuButton>
                  </div>
                  <MenuItems
                    transition
                    className="user-menu-item absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black ring-opacity-5 transition focus:outline-none data-[closed]:scale-95 data-[closed]:transform data-[closed]:opacity-0 data-[enter]:duration-100 data-[leave]:duration-75 data-[enter]:ease-out data-[leave]:ease-in"
                    data-testid="menu-items"
                  >
                    {profile.menuItems.map((item) => (
                      <MenuItem key={item.text}>
                        {({ focus }) => (
                          <a
                            data-testid={`menu-item-${item.text.replaceAll(/\s+/g, "-")}`}
                            href={item.href}
                            className={classNames(focus ? "bg-gray-100" : "", "block px-4 py-2 text-sm text-gray-700")}
                            onClick={() => {
                              if (item.onclick) item.onclick();
                            }}
                          >
                            {item.text}
                          </a>
                        )}
                      </MenuItem>
                    ))}
                  </MenuItems>
                </Menu>
              </div>
            </div>
          </div>

          <DisclosurePanel className="sm:hidden">
            <div className="nav-menu-items space-y-1 px-2 pb-3 pt-2" data-testid="nav-menu-items">
              {navigation.map((item) => (
                <DisclosureButton
                  key={item.name}
                  data-testid={`nav-menu-item-${item.name.replaceAll(/\s+/g, "-")}`}
                  as="a"
                  href={item.href}
                  className={classNames(
                    "nav-menu-item",
                    item.current ? "bg-gray-900 text-white" : "text-gray-300 hover:bg-gray-700 hover:text-white",
                    "block rounded-md px-3 py-2 text-base font-medium bbbbbbbb"
                  )}
                  aria-current={item.current ? "page" : undefined}
                >
                  {item.name}
                </DisclosureButton>
              ))}
            </div>
          </DisclosurePanel>
        </>
      )}
    </Disclosure>
  );
}
