interface HeaderProps {
  label: string;
}

export const Header = ({ label }: HeaderProps) => {
  return (
    <div className="card-header sm:mx-auto sm:w-full sm:max-w-sm">
      {/* <img className="mx-auto h-10 w-auto" src="https://tailwindui.com/img/logos/mark.svg?color=indigo&shade=600" alt="Your Company" /> */}
      <h2 className="mt-10 text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">{label}</h2>
    </div>
  );
};
