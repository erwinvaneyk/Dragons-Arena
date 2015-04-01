# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  config.vm.box = "ubuntu/trusty64"

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  config.vm.box_check_update = true

  config.vm.provider "virtualbox" do |vb|
     # Customize the amount of memory on the VM:
     vb.memory = "2048"
  end

  config.vm.provision :shell, path: "scripts/provision.sh"

  # Define machines
  config.vm.define "publicnode" do |publicnode|
     publicnode.vm.network "forwarded_port", guest: 2345, host: 2345
  end
  config.vm.define "node2", autostart: false
  config.vm.define "node3", autostart: false

end
