<template>
  <q-layout view="hHh LpR lFr">

    <q-header bordered class="bg-primary text-white">
      <q-toolbar>
        <img src="../../assets/images/duic200x60.png">
        <q-toolbar-title>
          配置中心
        </q-toolbar-title>

        <q-space />

        <q-input dark dense standout v-model="text" input-class="text-right" class="q-ml-md">
          <template v-slot:append>
            <q-icon v-if="text === ''" name="search" />
            <q-icon v-else name="clear" class="cursor-pointer" @click="text = ''" />
          </template>
        </q-input>

        <q-btn-toggle
          v-model="model"
          flat stretch
          toggle-color="yellow"
          :options="[
            {label: 'One', value: 'one'},
            {label: 'Two', value: 'two'},
            {label: 'Three', value: 'three'}
          ]"
        />
      </q-toolbar>
    </q-header>

    <q-page-container>
      <router-view />
    </q-page-container>

  </q-layout>
</template>
<script lang="ts" src="./index.ts" />
<style scoped lang="stylus" src="./index.styl" /> 
