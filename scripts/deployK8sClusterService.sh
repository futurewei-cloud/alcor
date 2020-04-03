#!/bin/bash

#Clean up
kubectl delete svc alcor
kubectl delete deployment alcor

#Create Namespace for ignite
kubectl apply -f kubernetes/db/ignite/ignite-namespace.yaml

#Create ConfigMap for ignite
kubectl create configmap ignite-config --from-file=config/ignite-config.xml --namespace=ignite
kubectl get cm -n ignite

#Create ServiceAccount for ignite
kubectl apply -f kubernetes/db/ignite/ignite-service-account.yaml
kubectl get sa -n ignite

#Create CLusterRole for ignite
kubectl apply -f kubernetes/db/ignite/ignite-cluster-role.yaml
kubectl get clusterrole -n ignite | grep ignite

#Create ClusterRoleBinding for ignite
kubectl apply -f kubernetes/db/ignite/ignite-cluster-role-binding.yaml
kubectl get clusterrolebinding -n ignite | grep ignite

#Create Service for ignite
kubectl apply -f kubernetes/db/ignite/ignite-service.yaml
kubectl get svc -o wide

#Create Deployment for ignite
kubectl apply -f kubernetes/db/ignite/ignite-deployment.yaml
kubectl get pod -o wide

kubectl apply -f kubernetes/app/controller-deployment.yaml
kubectl get deployments -o wide

kubectl expose deployment alcor --type=LoadBalancer --name=alcor
kubectl get svc -o wide
kubectl get po -A
